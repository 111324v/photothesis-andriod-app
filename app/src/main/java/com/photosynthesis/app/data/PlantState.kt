package com.photosynthesis.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 植物状态实体
 * 整个App只有一条记录（当前选择的植物）
 */
@Entity(tableName = "plant_state")
data class PlantState(
    @PrimaryKey
    val id: Int = 1, // 始终只有一条记录

    // 植物类型：sunflower / cactus / pine
    val plantType: String = "sunflower",

    // 累计光合值（主层，只增不减）
    val totalPhotosynthesis: Int = 0,

    // 当前生命阶段：seed / sprout / seedling / growing / blooming / eternal
    val growthStage: String = "seed",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)
