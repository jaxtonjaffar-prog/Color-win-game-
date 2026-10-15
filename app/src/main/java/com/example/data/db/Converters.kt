package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.BetStatus

class Converters {
    @TypeConverter
    fun fromBetStatus(status: BetStatus): String = status.name

    @TypeConverter
    fun toBetStatus(value: String): BetStatus = try {
        BetStatus.valueOf(value)
    } catch (e: Exception) {
        BetStatus.PENDING
    }
}
