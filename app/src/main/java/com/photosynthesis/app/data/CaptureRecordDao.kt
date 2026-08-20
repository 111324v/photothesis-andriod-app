package com.photosynthesis.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 拍照记录的数据库操作接口
 */
@Dao
interface CaptureRecordDao {

    // 插入一条新记录
    @Insert
    suspend fun insert(record: CaptureRecord): Long

    // 获取所有记录（按时间倒序），用 Flow 实现实时观察
    @Query("SELECT * FROM capture_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CaptureRecord>>

    // 获取指定日期范围内的记录（用于日历视图）
    @Query("SELECT * FROM capture_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<CaptureRecord>>

    // 获取最近7天的记录（用于计算多样性系数）
    @Query("SELECT * FROM capture_records WHERE timestamp >= :sevenDaysAgo ORDER BY timestamp DESC")
    suspend fun getRecentRecords(sevenDaysAgo: Long): List<CaptureRecord>

    // 获取所有记录的光合值总和（用于植物成长主层）
    @Query("SELECT COALESCE(SUM(photosynthesisValue), 0) FROM capture_records")
    fun getTotalPhotosynthesis(): Flow<Int>
}
