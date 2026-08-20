package com.photosynthesis.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.PlantState
import kotlinx.coroutines.launch

/**
 * 选择植物页
 * 对应原型 screen-choose：黄色渐变 + 横向轮播卡片
 * 三种植物：向日葵(sunflower) / 仙人掌(cactus) / 松树(pine)
 */

// 植物数据
data class PlantOption(
    val type: String,
    val name: String,
    val emoji: String,
    val description: String
)

private val plantOptions = listOf(
    PlantOption("cactus", "Cactus", "🌵", "Needs little. Built for those with irregular routines or indoor life."),
    PlantOption("sunflower", "Sunflower", "🌻", "Perfect for beginners or anyone looking for an easy-to-care-for plant."),
    PlantOption("pine", "Pine Tree", "🌲", "Tough wind and pressure. Perfect if you need stability and resilience.")
)

@Composable
fun ChoosePlantScreen(onPlantChosen: (String) -> Unit) {
    // 当前选中索引（默认向日葵=1）
    var currentIndex by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD900), // 明黄
                        Color(0xFFFEFADE)  // 浅黄
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部标题
            Column(
                modifier = Modifier.padding(start = 26.dp, top = 60.dp)
            ) {
                Text(
                    text = "Choose",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Your Plant",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )
            }

            // 斜体引言
            Text(
                text = "Every time you walk toward the light,\nI grow a little taller than yesterday.",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A6010),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 卡片区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                plantOptions.forEachIndexed { index, plant ->
                    val isActive = index == currentIndex
                    // 缩放动画
                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.8f,
                        animationSpec = tween(300),
                        label = "cardScale"
                    )
                    // 旋转动画
                    val rotation by animateFloatAsState(
                        targetValue = when {
                            index < currentIndex -> 10f
                            index > currentIndex -> -10f
                            else -> 0f
                        },
                        animationSpec = tween(300),
                        label = "cardRotation"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.72f,
                        animationSpec = tween(300),
                        label = "cardAlpha"
                    )

                    Card(
                        modifier = Modifier
                            .width(if (isActive) 240.dp else 180.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                rotationZ = rotation
                                this.alpha = alpha
                            }
                            .clickable { currentIndex = index },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isActive) 16.dp else 4.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // 植物图片占位（用emoji + 背景色块）
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF0F8E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = plant.emoji, fontSize = 64.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 植物名
                            Text(
                                text = plant.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // 描述
                            Text(
                                text = plant.description,
                                fontSize = 12.sp,
                                color = Color(0xFF999999),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // CTA 按钮（仅选中卡片可见）
                            if (isActive) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            // 保存选择的植物到数据库
                                            val db = PhotosynthesisApp.instance.database
                                            db.plantStateDao().upsert(
                                                PlantState(plantType = plant.type)
                                            )
                                            onPlantChosen(plant.type)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1A1A1A)
                                    ),
                                    shape = RoundedCornerShape(50.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                            .padding(3.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Choose this partner",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    if (index < plantOptions.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // 分页小圆点
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                plantOptions.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (index == currentIndex) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentIndex) Color(0xFF1A1A1A)
                                else Color(0x38000000)
                            )
                            .clickable { currentIndex = index }
                    )
                }
            }
        }
    }
}
