package com.photosynthesis.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.PlantState
import com.photosynthesis.app.ui.AssetLoader
import com.photosynthesis.app.ui.rememberSvgImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * "我的"页面 - 还原设计稿
 * 浅蓝背景 + 当前植物卡片 + 日历 + 周要素雷达图
 */
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val svgLoader = rememberSvgImageLoader()
    val scrollState = rememberScrollState()

    // 加载植物状态
    var plantState by remember { mutableStateOf<PlantState?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = (context.applicationContext as PhotosynthesisApp).database
            plantState = db.plantStateDao().getPlantState().firstOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB8E8F8))
            .verticalScroll(scrollState)
    ) {
        // 顶部导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1A1A1A),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() }
                    .padding(8.dp)
            )
            // 头像按钮
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = AssetLoader.svgRequest(context, "\u6211\u7684-icon.svg"),
                    contentDescription = "Avatar",
                    imageLoader = svgLoader,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Current Plants 标题
        Text(
            text = "Current Plants",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // 当前植物卡片（SVG 素材）
        AsyncImage(
            model = AssetLoader.svgRequest(context, "\u6211\u7684\u5361\u7247.svg"),
            contentDescription = "Current plant card",
            imageLoader = svgLoader,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        // My Plants 标题
        Text(
            text = "My Plants",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // 植物列表（SVG 素材）
        AsyncImage(
            model = AssetLoader.svgRequest(context, "myplant.svg"),
            contentDescription = "My plants",
            imageLoader = svgLoader,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth()
        )

        // Light Archive 标题
        Text(
            text = "Light Archive",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // 简化日历（复用 SVG 背景样式）
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // 星期标题
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                        Text(
                            text = d,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 模拟日历格子
                for (row in 0 until 4) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if ((row * 7 + col) % 3 == 0) Color(0xFFE3F5F5)
                                        else Color.White.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val day = row * 7 + col + 1
                                if (day <= 28) {
                                    Text(
                                        "$day",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Weekly Elements 标题
        Text(
            text = "Weekly Elements",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // 周要素卡片
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 四要素柱状图
                val elements = listOf(
                    Triple("Sun", Color(0xFFFFD97D), 0.8f),
                    Triple("Water", Color(0xFF71D4F3), 0.52f),
                    Triple("Sky", Color(0xFF74B9FF), 0.7f),
                    Triple("Nature", Color(0xFF7DD4A8), 0.88f)
                )

                elements.forEach { (name, color, progress) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 圆点
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 名称
                        Text(
                            name,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.width(50.dp)
                        )
                        // 进度条
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Black.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // 数值
                        Text(
                            "${(progress * 4).let { "%.1f".format(it) }}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
