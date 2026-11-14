package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AccountEntity::class,
        TxnEntity::class,
        DebtEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HisabDatabase : RoomDatabase() {
    abstract fun hisabDao(): HisabDao

    companion object {
        @Volatile
        private var INSTANCE: HisabDatabase? = null

        fun getInstance(context: Context): HisabDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HisabDatabase::class.java,
                    "hisab_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
