package com.mxj.mmitest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 应用数据库
 * 包含测试结果和测试会话表
 */
@Database(
    entities = [TestResultEntity::class, TestSessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TestResultConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun testResultDao(): TestResultDao
    abstract fun testSessionDao(): TestSessionDao

    companion object {
        private const val DATABASE_NAME = "mmitest_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例（单例模式）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // 版本升级时删除旧表
                .build()
        }
    }
}
