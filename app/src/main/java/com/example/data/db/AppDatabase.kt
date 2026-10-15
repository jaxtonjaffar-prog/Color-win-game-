package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.DrawRound
import com.example.data.model.GameSettings
import com.example.data.model.TransactionRecord
import com.example.data.model.UserBet
import com.example.data.model.UserProfile
import com.example.data.model.WalletAccount

@Database(
    entities = [
        WalletAccount::class,
        UserProfile::class,
        GameSettings::class,
        DrawRound::class,
        UserBet::class,
        TransactionRecord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "color_win_betting.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
