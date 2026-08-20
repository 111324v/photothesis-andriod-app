package com.photosynthesis.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 拍照记录实体
 * 每次拍照并分析后存一条记录
 */
@Entity(tableName = "capture_records")
data class CaptureRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 照片本地路径
    val photoPath: String,

    // 拍摄时间戳（毫秒）
    val timestamp: Long,

    // 四要素评分，各 0-3 分
    val lightScore: Int,   // 光
    val waterScore: Int,   // 水
    val airScore: Int,     // 气
    val biomeScore: Int,   // 境

    // 本次光合值 = 光+水+气+境（0-12）
    val photosynthesisValue: Int
)
