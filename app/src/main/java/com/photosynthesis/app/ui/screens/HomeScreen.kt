package com.photosynthesis.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.PlantGrowthEngine
import com.photosynthesis.app.data.PlantState
import com.photosynthesis.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 主页
 * 对应原型 screen-home：天空背景 + 植物展示 + 独白气泡 + FAB菜单
 */
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val db = PhotosynthesisApp.instance.database
    val plantState by db.plantStateDao().getPlantState().collectAsState(initial = null)
    val totalPhotosynthesis by db.captureRecordDao().getTotalPhotosynthesis().collectAsState(initial = 0)

    // FAB 展开状态
    var fabExpanded by remember { mutableStateOf(false) }

    // 植物呼吸动画（轻微缩放模拟微风）
    val infiniteTransition = rememberInfiniteTransition(label = "plantBreeze")
    val plantScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plantScale"
    )

    // 计算生长阶段
    val currentStage = PlantGrowthEngine.calculateStage(totalPhotosynthesis)
    val currentPlant = plantState ?: PlantState()

    // 植物 emoji 映射
    val plantEmoji = when (currentPlant.plantType) {
        "sunflower" -> when (currentStage) {
            PlantGrowthEngine.GrowthStage.SEED -> "🫘"
            PlantGrowthEngine.GrowthStage.SPROUT -> "🌱"
            PlantGrowthEngine.GrowthStage.SEEDLING -> "🪴"
            PlantGrowthEngine.GrowthStage.GROWING -> "🌿"
            PlantGrowthEngine.GrowthStage.BLOOMING -> "🌻"
            PlantGrowthEngine.GrowthStage.ETERNAL -> "🌻✨"
        }
        "cactus" -> when (currentStage) {
            PlantGrowthEngine.GrowthStage.SEED -> "🫘"
            PlantGrowthEngine.GrowthStage.SPROUT -> "🌱"
            PlantGrowthEngine.GrowthStage.SEEDLING -> "🪴"
            PlantGrowthEngine.GrowthStage.GROWING -> "🌵"
            PlantGrowthEngine.GrowthStage.BLOOMING -> "🌵🌸"
            PlantGrowthEngine.GrowthStage.ETERNAL -> "🌵✨"
        }
        "pine" -> when (currentStage) {
            PlantGrowthEngine.GrowthStage.SEED -> "🫘"
            PlantGrowthEngine.GrowthStage.SPROUT -> "🌱"
            PlantGrowthEngine.GrowthStage.SEEDLING -> "🪴"
            PlantGrowthEngine.GrowthStage.GROWING -> "🌲"
            PlantGrowthEngine.GrowthStage.BLOOMING -> "🎄"
            PlantGrowthEngine.GrowthStage.ETERNAL -> "🌲✨"
        }
        else -> "🌱"
    }

    // 独白文案
    val monologueText = when (currentStage) {
        PlantGrowthEngine.GrowthStage.SEED -> "I'm just a little seed... waiting for your first light ☀️"
        PlantGrowthEngine.GrowthStage.SPROUT -> "I can feel the warmth! Keep going 🌤️"
        PlantGrowthEngine.GrowthStage.SEEDLING -> "Growing stronger with every photo you take 💚"
        PlantGrowthEngine.GrowthStage.GROWING -> "Look how tall I've become! 🌿"
        PlantGrowthEngine.GrowthStage.BLOOMING -> "Every bit of sunlight this week — I received it all ✨"
        PlantGrowthEngine.GrowthStage.ETERNAL -> "We've been through so much together 🌟"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 天空渐变背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7DD8EE), // 天空蓝
                            Color(0xFF9EE0D0), // 过渡色
                            Color(0xFF6AB84A)  // 草地绿
                        )
                    )
                )
        )

        // 植物展示（居中大 emoji）
        Text(
            text = plantEmoji,
            fontSize = 120.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(plantScale)
                .offset(y = 40.dp)
        )

        // 顶部标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = currentPlant.plantType.replaceFirstChar { it.uppercase() },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0C3318)
                )
                Text(
                    text = currentStage.displayName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A4A22),
                    letterSpacing = 5.sp
                )
            }

            // 右上角「我的」按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x66FFFFFF))
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "我的",
                    tint = Color(0xFF0C3318),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 独白对话框
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 200.dp)
                .padding(horizontal = 50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xCCFFFFFF)
            )
        ) {
            Text(
                text = monologueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF074066),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        // 左下角植物信息卡
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 100.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xCC040E08)
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = currentPlant.plantType.replaceFirstChar { it.uppercase() },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.Baseline) {
                    Text("光合值 ", fontSize = 11.sp, color = Color(0x80FFFFFF))
                    Text(
                        text = "$totalPhotosynthesis",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LightGreen
                    )
                    Text(" lux", fontSize = 11.sp, color = Color(0x73FFFFFF))
                }
            }
        }

        // FAB 按钮区域（右下角）
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 48.dp)
        ) {
            // 展开后的子按钮
            if (fabExpanded) {
                // 拍摄按钮（左侧）
                FloatingActionButton(
                    onClick = { fabExpanded = false; onNavigateToCamera() },
                    containerColor = Color(0xCC0A1A0E),
                    modifier = Modifier
                        .offset(x = (-80).dp, y = 0.dp)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, "拍摄", tint = Color.White)
                }

                // 光档案按钮（上方）
                FloatingActionButton(
                    onClick = { fabExpanded = false; onNavigateToArchive() },
                    containerColor = Color(0xCC0A1A0E),
                    modifier = Modifier
                        .offset(x = 0.dp, y = (-80).dp)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.GridView, "档案", tint = Color.White)
                }
            }

            // 主 FAB
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = Color(0xFF1A1A1A),
                modifier = Modifier.size(64.dp)
            ) {
                Text(
                    text = if (fabExpanded) "✕" else "☀️",
                    fontSize = 24.sp
                )
            }
        }

        // FAB 展开时的透明遮罩（点击关闭）
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { fabExpanded = false }
            )
        }
    }
}
