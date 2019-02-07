package com.nkrin.treclock.data.room

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun toDuration(value: Long): Duration {
        return Duration.ofMinutes(value)
    }

    @TypeConverter
    fun fromDuration(duration: Duration): Long {
        return duration.toMinutes()
    }
}