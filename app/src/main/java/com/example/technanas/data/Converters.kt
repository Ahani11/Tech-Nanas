package com.example.technanas.data

import androidx.room.TypeConverter
import com.example.technanas.data.model.AnnouncementType
import com.example.technanas.data.model.FAQCategory

class Converters {

    @TypeConverter
    fun fromAnnouncementType(type: AnnouncementType): String = type.name

    @TypeConverter
    fun toAnnouncementType(value: String): AnnouncementType =
        AnnouncementType.valueOf(value)

    @TypeConverter
    fun fromFAQCategory(cat: FAQCategory): String = cat.name

    @TypeConverter
    fun toFAQCategory(value: String): FAQCategory =
        FAQCategory.valueOf(value)
}
