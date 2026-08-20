package com.photosynthesis.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.photosynthesis.app.ui.AssetLoader

/**
 * 选择植物页 - 还原设计稿
 * 黄色渐变背景 + 横向轮播卡片（仙人掌/向日葵/松树）
 * 卡片内含植物图片 + 名称 + 描述 + CTA按钮
 */
@Composable
fun ChoosePlantScreen(onPlantChosen: (String) -> Unit) {
    val context = LocalContext.current

    // 当前选中的植物索引（0=仙人掌, 1=向日葵, 2=松树）
    var currentIndex by remember { mutableIntStateOf(1) }

    // 植物数据
    val plants = remember {
        listOf(
            PlantChoice(
                id = "cactus",
                name = "cactus",
                desc = "Needs little. Built for those with irregular routines or indoor life.",
                imageFile = "\u4ed9\u4eba\u638c.png"  // 仙人掌.png
            ),
            PlantChoice(
                id = "sunflower",
                name = "sunflower",
                desc = "Perfect for beginners or anyone looking for an easy-to-care-for plant.",
                imageFile = "\u9009\u62e9\u690d\u7269\u9875-\u5411\u65e5\u8475\u5361\u7247.png"  // 选择植物页-向日葵卡片.png
            ),
            PlantChoice(
                id = "pine",
                name = "pine tree",
                desc = "Tough wind and pressure. Perfect if you need stability and resilience.",
                imageFile = "\u677e\u6811.png"  // 松树.png
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD900), Color(0xFFFEFADE))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(60.dp))

            // 顶部标题
            Column(modifier = Modifier.padding(horizontal = 26.dp)) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // 斜体引言
            Text(
                text = "Every time you walk toward the light,\nI grow a little taller than yesterday.",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A6010),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 卡片区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PlantCard(
                    plant = plants[currentIndex],
                    context = context,
                    onChoose = { onPlantChosen(plants[currentIndex].id) }
                )
            }

            // 分页圆点
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                plants.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (index == currentIndex) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) Color(0xFF1A1A1A)
                                else Color(0x38000000)
                            )
                            .clickable { currentIndex = index }
                    )
                }
            }
        }

        // 左右滑动区域（简化：点击左/右半屏切换）
        Row(modifier = Modifier.fillMaxSize()) {
            // 左半屏 - 上一张
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        if (currentIndex > 0) currentIndex--
                    }
            )
            // 右半屏 - 下一张
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                        if (currentIndex < plants.size - 1) currentIndex++
                    }
            )
        }
    }
}

/**
 * 单张植物卡片组件
 */
@Composable
private fun PlantCard(
    plant: PlantChoice,
    context: android.content.Context,
    onChoose: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // 植物图片
            AsyncImage(
                model = AssetLoader.imageRequest(context, plant.imageFile),
                contentDescription = plant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // 卡片文字区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = plant.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = plant.desc,
                    fontSize = 13.sp,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // CTA 按钮 - 黑色胶囊
                Button(
                    onClick = onChoose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                ) {
                    // 绿色圆形箭头
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("→", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose this partner",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/** 植物选项数据类 */
private data class PlantChoice(
    val id: String,
    val name: String,
    val desc: String,
    val imageFile: String
)
