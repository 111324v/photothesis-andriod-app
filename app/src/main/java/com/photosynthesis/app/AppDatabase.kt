package com.photosynthesis.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.data.CaptureRecordDao
import com.photosynthesis.app.data.PlantState
import com.photosynthesis.app.data.PlantStateDao

/**
 * Room 数据库定义
 * 只包含两张表：拍照记录 + 植物状态
 */
@Database(
    entities = [CaptureRecord::class, PlantState::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun captureRecordDao(): CaptureRecordDao
    abstract fun plantStateDao(): PlantStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photosynthesis.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
