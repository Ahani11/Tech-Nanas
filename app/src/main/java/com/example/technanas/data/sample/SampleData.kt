package com.example.technanas.data.sample

import com.example.technanas.data.model.Announcement
import com.example.technanas.data.model.AnnouncementType
import com.example.technanas.data.model.FAQ
import com.example.technanas.data.model.FAQCategory

object SampleData {

    fun sampleAnnouncements(now: Long): List<Announcement> {
        return listOf(
            Announcement(
                title = "Welcome to TechNanas",
                shortDescription = "Introduction to the platform",
                fullDescription = "TechNanas helps pineapple entrepreneurs access useful information, announcements and FAQs.",
                type = AnnouncementType.GENERAL,
                dateMillis = now
            ),
            Announcement(
                title = "Current Pineapple Price",
                shortDescription = "Price update for this week",
                fullDescription = "The indicative farm-gate price for pineapples this week is RM 1.20 per kg. Prices may vary by region.",
                type = AnnouncementType.PRICE,
                dateMillis = now - 86400000L
            ),
            Announcement(
                title = "Training: Farm Management",
                shortDescription = "One-day training program",
                fullDescription = "There will be a one-day training program on pineapple farm management. Contact your local office.",
                type = AnnouncementType.TRAINING,
                dateMillis = now - 2 * 86400000L
            )
        )
    }

    fun sampleFaqs(): List<FAQ> {
        return listOf(
            FAQ(
                question = "What is TechNanas?",
                answer = "TechNanas is an app that supports pineapple entrepreneurs with announcements, FAQs and useful links.",
                category = FAQCategory.GENERAL,
                keywords = "what is technanas app platform pineapple"
            ),
            FAQ(
                question = "How do I register an account?",
                answer = "On the login screen, tap Register, fill in your details and submit. If your email is already used, you must choose another email.",
                category = FAQCategory.ACCOUNT,
                keywords = "register account create sign up email password"
            ),
            FAQ(
                question = "How can I update my farm information?",
                answer = "Open the Profile from the top menu, then tap Edit Profile. You can update your farm name, size and address.",
                category = FAQCategory.ACCOUNT,
                keywords = "update profile farm information edit"
            ),
            FAQ(
                question = "Where can I see pineapple price information?",
                answer = "Go to the Announcements tab and look for items tagged as Price. They contain the latest price information.",
                category = FAQCategory.PRICE,
                keywords = "pineapple price latest announcements price info"
            )
        )
    }
}
