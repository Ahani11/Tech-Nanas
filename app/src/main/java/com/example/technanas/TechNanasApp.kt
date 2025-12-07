package com.example.technanas

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.technanas.data.AppDatabase
import com.example.technanas.data.model.User
import com.example.technanas.data.repository.AnnouncementRepository
import com.example.technanas.data.repository.FAQRepository
import com.example.technanas.data.repository.FarmRepository
import com.example.technanas.data.repository.UserRepository
import com.example.technanas.session.SessionManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.technanas.data.remote.FaqChatApi
import com.example.technanas.data.repository.LlmFaqChatRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TechNanasApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    // User repo now uses Room + Firestore
    val userRepository: UserRepository by lazy {
        UserRepository(
            database.userDao(),
            Firebase.firestore
        )
    }

    // Announcement repo uses Room + Firestore (unchanged logic)
    val announcementRepository: AnnouncementRepository by lazy {
        AnnouncementRepository(
            announcementDao = database.announcementDao(),
            firestore = Firebase.firestore
        )
    }

    val faqRepository: FAQRepository by lazy { FAQRepository(database.faqDao()) }

    // Farm repo now uses Room + Firestore
    val farmRepository: FarmRepository by lazy {
        FarmRepository(
            database.farmDao(),
            Firebase.firestore
        )
    }


    // --- LLM FAQ Chat (Retrofit + repository) ---

    // For Android emulator talking to backend on your PC:
    // If Flask runs at http://127.0.0.1:5000 on your PC,
    // the emulator must use http://10.0.2.2:5000/
    private val faqChatBaseUrl = "http://172.20.10.12:5000/"

    private val faqChatHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val faqChatRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(faqChatBaseUrl)
            .client(faqChatHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val faqChatApi: FaqChatApi by lazy {
        faqChatRetrofit.create(FaqChatApi::class.java)
    }

    val llmFaqChatRepository: LlmFaqChatRepository by lazy {
        LlmFaqChatRepository(faqChatApi)
    }



    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 1) Apply saved theme
        applySavedTheme()

        // 2) Init session manager
        sessionManager = SessionManager(this)

        // 3) Seed / sync DB
        appScope.launch {
            prepopulateDatabase()
        }
    }

    // Read the saved toggle and apply night / light mode
    private fun applySavedTheme() {
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("night_mode", false)

        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private suspend fun prepopulateDatabase() {
        val faqDao = database.faqDao()
        val userDao = database.userDao()

        // 🔹 Sync announcements from Firestore into Room cache
        try {
            announcementRepository.refreshFromRemote()
        } catch (e: Exception) {
            // If offline, list will stay as-is.
        }

        // 🔹 Seed FAQs locally if empty (still using SampleData)
        if (faqDao.getCount() == 0) {
            val sampleFaqs = com.example.technanas.data.sample.SampleData.sampleFaqs()
            faqDao.insertAll(sampleFaqs)
        }

    }
}
