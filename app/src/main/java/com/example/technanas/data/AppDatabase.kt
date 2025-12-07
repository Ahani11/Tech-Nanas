package com.example.technanas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.technanas.data.dao.AnnouncementDao
import com.example.technanas.data.dao.FAQDao
import com.example.technanas.data.dao.FarmDao
import com.example.technanas.data.dao.UserDao
import com.example.technanas.data.model.Announcement
import com.example.technanas.data.model.FAQ
import com.example.technanas.data.model.Farm
import com.example.technanas.data.model.User

@Database(
    entities = [User::class, Announcement::class, FAQ::class, Farm::class],
    version = 7 ,          // 🔺 was 3, bump to 4
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun faqDao(): FAQDao
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tech_nanas.db"
                )
                    .fallbackToDestructiveMigration()   // drop & recreate on version change
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
