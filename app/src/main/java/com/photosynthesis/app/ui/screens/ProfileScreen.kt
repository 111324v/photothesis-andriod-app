package com.photosynthesis.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.data.PlantGrowthEngine
import com.photosynthesis.app.data.PlantState
import com.photosynthesis.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 个人页
 * 对应原型 screen-profile：我的植物 + 光档案日历 + 周要素雷达
 */
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = PhotosynthesisApp.instance.database
    val plantState by db.plantStateDao().getPlantState().collectAsState(initial = null)
    val totalPhotosynthesis by db.captureRecordDao().getTotalPhotosynthesis().collectAsState(initial = 0)
    val allRecords by db.captureRecordDao().getAllRecords().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // API 配置弹窗
    var showSettings by remember { mutableStateOf(false) }

    val currentPlant = plantState ?: PlantState()
    val currentStage = PlantGrowthEngine.calculateStage(totalPhotosynthesis)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB8E8F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部导航
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
                // 设置按钮（配置API Key）
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { showSettings = true }
                )
            }

            // 当前植物卡片
            Text(
                text = "Current Plant",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 植物 emoji
                    val emoji = when (currentPlant.plantType) {
                        "sunflower" -> "🌻"
                        "cactus" -> "🌵"
                        "pine" -> "🌲"
                        else -> "🌱"
                    }
                    Text(text = emoji, fontSize = 48.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentPlant.plantType.replaceFirstChar { it.uppercase() },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "${currentStage.displayName} · 光合值 $totalPhotosynthesis",
                            fontSize = 13.sp,
                            color = Color(0x991A1A1A)
                        )
                        // 进度条到下一阶段
                        val nextStage = PlantGrowthEngine.GrowthStage.entries
                            .firstOrNull { it.threshold > totalPhotosynthesis }
                        if (nextStage != null) {
                            val prevThreshold = currentStage.threshold
                            val progress = (totalPhotosynthesis - prevThreshold).toFloat() /
                                    (nextStage.threshold - prevThreshold).toFloat()
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LightGreen,
                                trackColor = Color(0x1A000000)
                            )
                            Text(
                                text = "距离${nextStage.displayName}还需 ${nextStage.threshold - totalPhotosynthesis} 光合值",
                                fontSize = 11.sp,
                                color = Color(0x661A1A1A),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 本周要素统计
            Text(
                text = "Weekly Elements",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            WeeklyElementsCard(records = allRecords)

            Spacer(modifier = Modifier.height(20.dp))

            // 统计数据
            Text(
                text = "Stats",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    StatRow("总拍摄次数", "${allRecords.size}")
                    StatRow("累计光合值", "$totalPhotosynthesis")
                    StatRow("当前阶段", currentStage.displayName)
                    StatRow("植物类型", currentPlant.plantType.replaceFirstChar { it.uppercase() })
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // API 设置弹窗
    if (showSettings) {
        ApiSettingsDialog(
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0x991A1A1A))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
    }
}

/**
 * 本周四要素统计卡片
 */
@Composable
private fun WeeklyElementsCard(records: List<CaptureRecord>) {
    // 取最近7天的记录
    val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
    val recentRecords = records.filter { it.timestamp >= sevenDaysAgo }

    val totalLight = recentRecords.sumOf { it.lightScore }
    val totalWater = recentRecords.sumOf { it.waterScore }
    val totalAir = recentRecords.sumOf { it.airScore }
    val totalBiome = recentRecords.sumOf { it.biomeScore }
    val maxVal = maxOf(totalLight, totalWater, totalAir, totalBiome, 1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ElementBarRow("☀️ Sun", totalLight, maxVal, ElementLight)
            ElementBarRow("💧 Water", totalWater, maxVal, ElementWater)
            ElementBarRow("🌤️ Sky", totalAir, maxVal, ElementAir)
            ElementBarRow("🌳 Nature", totalBiome, maxVal, ElementBiome)
        }
    }
}

@Composable
private fun ElementBarRow(label: String, value: Int, maxVal: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xB31A1A1A),
            modifier = Modifier.width(90.dp)
        )
        LinearProgressIndicator(
            progress = { value.toFloat() / maxVal.toFloat() },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0x1A000000)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$value",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.width(30.dp)
        )
    }
}

/**
 * API 设置弹窗
 * 用户在这里配置大模型的 API Key、URL、Model
 */
@Composable
private fun ApiSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("photosynthesis_config", android.content.Context.MODE_PRIVATE)

    var apiKey by remember { mutableStateOf(prefs.getString("api_key", "") ?: "") }
    var apiUrl by remember { mutableStateOf(prefs.getString("api_url", "https://api.openai.com/v1/chat/completions") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("API 设置", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("配置大模型 API 以启用照片分析功能", fontSize = 13.sp, color = Color.Gray)

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("API URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    placeholder = { Text("gpt-4o-mini") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // 保存配置
                prefs.edit()
                    .putString("api_key", apiKey)
                    .putString("api_url", apiUrl)
                    .putString("model", model)
                    .apply()
                onDismiss()
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
