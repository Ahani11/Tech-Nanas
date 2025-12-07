package com.example.technanas.data.repository

import com.example.technanas.data.dao.FAQDao
import com.example.technanas.data.model.FAQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class FAQRepository(private val faqDao: FAQDao) {

    /**
     * Expose all FAQs as a Flow for any UI that wants to observe them.
     */
    fun getAllFlow() = faqDao.getAll()

    /**
     * Simple greeting message shown when the user opens the FAQ chat.
     */
    suspend fun getGreetingResponse(): String = withContext(Dispatchers.Default) {
        "Hello! I am the TechNanas FAQ assistant. Ask me about your account, pineapple prices, farming tips, or LPNM support. 🍍"
    }

    /**
     * Core chatbot logic:
     *  - reads FAQs from Room
     *  - scores them against the user's question
     *  - returns the best answer
     *  - if not very confident, includes suggested related questions
     *
     *  selectedCategory is kept for future use but currently ignored.
     */
    suspend fun getAnswerFor(
        question: String,
        selectedCategory: String? = null   // currently not used, kept for future aspects
    ): String = withContext(Dispatchers.IO) {
        val raw = question.trim()

        if (raw.isEmpty()) {
            return@withContext "Please type your question first."
        }

        // Normalize user input
        val tokens = tokenize(raw)
        if (tokens.isEmpty()) {
            return@withContext "Please try asking in a simpler way."
        }

        // Load all FAQs from Room (seeded locally or later from Firestore)
        val faqs: List<FAQ> = try {
            faqDao.getAllOnce()
        } catch (e: Exception) {
            emptyList()
        }

        if (faqs.isEmpty()) {
            return@withContext "FAQ data is not available yet. Please check your connection or try again later."
        }

        // Score all FAQs
        val scoredFaqs = faqs.map { faq ->
            faq to scoreFaq(faq, tokens)
        }

        val maxScore = scoredFaqs.maxOfOrNull { it.second } ?: 0

        // If we really didn't match anything
        if (maxScore <= 0) {
            val topSuggestions = faqs.take(3)
            return@withContext buildNoMatchResponse(topSuggestions)
        }

        // Sort by score (best first)
        val sorted = scoredFaqs.sortedByDescending { it.second }

        val bestFaq = sorted.first().first
        val otherSuggestions = sorted.drop(1).take(3).map { it.first }

        buildMatchResponse(bestFaq, otherSuggestions)
    }

    /**
     * Break a question into normalized tokens (lowercased words without punctuation).
     */
    private fun tokenize(text: String): List<String> {
        return text
            .toLowerCaseCompat()
            .split("\\s+".toRegex())
            .map { token ->
                // Remove leading/trailing punctuation like ?,.,!
                token.trim { !it.isLetterOrDigit() }
            }
            .filter { it.length > 2 } // ignore very short words like "a", "is"
    }

    /**
     * Compute a simple score for how well an FAQ matches the given tokens.
     *
     * - Matches in the FAQ.question get +2
     * - Matches in the FAQ.keywords get +3 (keywords are more "searchable")
     */
    private fun scoreFaq(
        faq: FAQ,
        tokens: List<String>
    ): Int {
        var score = 0

        val questionText = faq.question.toLowerCaseCompat()
        val keywordsText = faq.keywords.toLowerCaseCompat()

        for (token in tokens) {
            if (questionText.contains(token)) {
                score += 2
            }
            if (keywordsText.contains(token)) {
                score += 3
            }
        }

        return score
    }

    /**
     * Build response text when we found a good FAQ and also some suggestions.
     */
    private fun buildMatchResponse(bestFaq: FAQ, suggestions: List<FAQ>): String {
        val sb = StringBuilder()
        sb.append(bestFaq.answer.trim())

        if (suggestions.isNotEmpty()) {
            sb.append("\n\nYou may also be interested in:")
            suggestions.forEachIndexed { index, faq ->
                sb.append("\n${index + 1}. ${faq.question}")
            }
        }

        return sb.toString()
    }

    /**
     * Build response text when we couldn't find a strong match.
     */
    private fun buildNoMatchResponse(suggestions: List<FAQ>): String {
        if (suggestions.isEmpty()) {
            return "Sorry, I am not sure how to answer that. Please try asking in a simpler way or contact support."
        }

        val sb = StringBuilder()
        sb.append("Sorry, I am not sure how to answer that exactly.")
        sb.append("\nMaybe these topics can help you:")

        suggestions.forEachIndexed { index, faq ->
            sb.append("\n${index + 1}. ${faq.question}")
        }

        sb.append("\n\nYou can type one of the questions above, or try to ask in a simpler way.")
        return sb.toString()
    }

    /**
     * Small helper to avoid any Kotlin stdlib version issues.
     * Works on any Java/Kotlin String version.
     */
    private fun String.toLowerCaseCompat(): String =
        lowercase(Locale.ROOT)
}
