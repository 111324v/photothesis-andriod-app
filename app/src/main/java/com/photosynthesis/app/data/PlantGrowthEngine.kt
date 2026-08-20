package com.photosynthesis.app.data

/**
 * 植物生长引擎
 * 核心逻辑：累计光合值 → 生命阶段（只增不减）
 * 子层逻辑：近7天四要素分布 → 心情态（动态可逆）
 */
object PlantGrowthEngine {

    /**
     * 生命阶段枚举及其所需累计光合值阈值
     * seed(种子) → sprout(萌芽) → seedling(幼苗) → growing(茁壮) → blooming(开花) → eternal(永生)
     */
    enum class GrowthStage(val threshold: Int, val displayName: String) {
        SEED(0, "种子"),
        SPROUT(10, "萌芽"),
        SEEDLING(30, "幼苗"),
        GROWING(80, "茁壮"),
        BLOOMING(150, "开花"),
        ETERNAL(300, "永生")
    }

    /**
     * 心情态枚举（子层，基于近7天要素分布）
     */
    enum class MoodState(val displayName: String, val englishName: String) {
        RADIANT("光芒四射", "Radiant"),       // 四要素均衡且充足
        SUNNY("阳光明媚", "Sunny"),           // 光要素突出
        MISTY("烟雨朦胧", "Misty"),           // 水要素突出
        BREEZY("清风徐来", "Breezy"),         // 气要素突出
        LUSH("郁郁葱葱", "Lush"),             // 境要素突出
        THIRSTY("渴望雨露", "Thirsty"),       // 水要素不足
        SHADED("期待阳光", "Shaded"),         // 光要素不足
        STILL("渴望微风", "Still"),            // 气要素不足
        LONELY("想换换环境", "Lonely"),        // 境要素不足
        DROWSY("有点困了", "Drowsy"),          // 所有要素都低
        BALANCED("心满意足", "Balanced")       // 完美均衡
    }

    /**
     * 根据累计光合值计算当前生长阶段
     */
    fun calculateStage(totalPhotosynthesis: Int): GrowthStage {
        return GrowthStage.entries
            .sortedByDescending { it.threshold }
            .first { totalPhotosynthesis >= it.threshold }
    }

    /**
     * 计算多样性系数（近7天四要素的均衡程度）
     * 范围：0.6 ~ 1.3
     * 四要素越均衡，系数越高
     */
    fun calculateDiversityCoefficient(recentRecords: List<CaptureRecord>): Float {
        if (recentRecords.isEmpty()) return 1.0f

        // 统计近7天各要素总分
        val totalLight = recentRecords.sumOf { it.lightScore }.toFloat()
        val totalWater = recentRecords.sumOf { it.waterScore }.toFloat()
        val totalAir = recentRecords.sumOf { it.airScore }.toFloat()
        val totalBiome = recentRecords.sumOf { it.biomeScore }.toFloat()

        val sum = totalLight + totalWater + totalAir + totalBiome
        if (sum == 0f) return 0.6f

        // 计算各要素占比，理想情况下每个都是25%
        val ratios = listOf(totalLight / sum, totalWater / sum, totalAir / sum, totalBiome / sum)
        // 计算与理想分布的偏差（标准差）
        val ideal = 0.25f
        val variance = ratios.map { (it - ideal) * (it - ideal) }.average().toFloat()
        val stdDev = Math.sqrt(variance.toDouble()).toFloat()

        // 标准差越小（越均衡）系数越高，映射到 0.6~1.3
        // stdDev范围约 0(完美均衡) ~ 0.25(极度不均)
        val coefficient = 1.3f - (stdDev / 0.25f) * 0.7f
        return coefficient.coerceIn(0.6f, 1.3f)
    }

    /**
     * 根据近7天要素分布计算心情态
     */
    fun calculateMood(recentRecords: List<CaptureRecord>): MoodState {
        if (recentRecords.isEmpty()) return MoodState.DROWSY

        val totalLight = recentRecords.sumOf { it.lightScore }
        val totalWater = recentRecords.sumOf { it.waterScore }
        val totalAir = recentRecords.sumOf { it.airScore }
        val totalBiome = recentRecords.sumOf { it.biomeScore }
        val total = totalLight + totalWater + totalAir + totalBiome

        // 所有要素都很低
        if (total < 5) return MoodState.DROWSY

        // 计算各要素占比
        val lightRatio = totalLight.toFloat() / total
        val waterRatio = totalWater.toFloat() / total
        val airRatio = totalAir.toFloat() / total
        val biomeRatio = totalBiome.toFloat() / total

        // 完美均衡：每个要素占比在20%-30%之间
        val allBalanced = listOf(lightRatio, waterRatio, airRatio, biomeRatio)
            .all { it in 0.2f..0.3f }
        if (allBalanced && total >= 20) return MoodState.BALANCED
        if (allBalanced) return MoodState.RADIANT

        // 某要素突出（>40%）
        if (lightRatio > 0.4f) return MoodState.SUNNY
        if (waterRatio > 0.4f) return MoodState.MISTY
        if (airRatio > 0.4f) return MoodState.BREEZY
        if (biomeRatio > 0.4f) return MoodState.LUSH

        // 某要素不足（<10%）
        if (lightRatio < 0.1f) return MoodState.SHADED
        if (waterRatio < 0.1f) return MoodState.THIRSTY
        if (airRatio < 0.1f) return MoodState.STILL
        if (biomeRatio < 0.1f) return MoodState.LONELY

        return MoodState.RADIANT
    }

    /**
     * 物种消化系数（不同植物对不同要素的偏好）
     * 返回四个系数 [光, 水, 气, 境]
     */
    fun getDigestionCoefficients(plantType: String): FloatArray {
        return when (plantType) {
            "sunflower" -> floatArrayOf(1.3f, 1.0f, 1.0f, 0.9f) // 向日葵偏好光
            "cactus" -> floatArrayOf(1.1f, 0.8f, 1.2f, 1.1f)    // 仙人掌耐旱偏气
            "pine" -> floatArrayOf(0.9f, 1.1f, 1.1f, 1.3f)      // 松树偏好环境
            else -> floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
        }
    }

    /**
     * 计算单次拍照的最终光合值
     * = (光×光系数 + 水×水系数 + 气×气系数 + 境×境系数) × 多样性系数
     */
    fun calculateFinalValue(
        lightScore: Int,
        waterScore: Int,
        airScore: Int,
        biomeScore: Int,
        plantType: String,
        diversityCoefficient: Float
    ): Int {
        val coefficients = getDigestionCoefficients(plantType)
        val rawValue = lightScore * coefficients[0] +
                waterScore * coefficients[1] +
                airScore * coefficients[2] +
                biomeScore * coefficients[3]
        return (rawValue * diversityCoefficient).toInt()
    }
}
