package com.photosynthesis.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 植物状态的数据库操作接口
 */
@Dao
interface PlantStateDao {

    // 获取当前植物状态（实时观察）
    @Query("SELECT * FROM plant_state WHERE id = 1")
    fun getPlantState(): Flow<PlantState?>

    // 插入或替换植物状态（选择植物时调用）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PlantState)

    // 更新植物累计光合值和生长阶段
    @Query("UPDATE plant_state SET totalPhotosynthesis = :total, growthStage = :stage WHERE id = 1")
    suspend fun updateGrowth(total: Int, stage: String)
}
