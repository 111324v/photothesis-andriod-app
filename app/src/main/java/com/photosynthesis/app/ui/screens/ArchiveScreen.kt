package com.photosynthesis.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 光档案页
 * 对应原型 screen-archive：日历视图 + 照片卡片 + 四要素进度条
 */
@Composable
fun ArchiveScreen(onBack: () -> Unit) {
    val db = PhotosynthesisApp.instance.database
    val allRecords by db.captureRecordDao().getAllRecords().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)) // 天蓝色背景
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                Spacer(modifier = Modifier.weight(1f))
            }

            // 标题
            Text(
                text = "Light Archive",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 22.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 日期显示
            Text(
                text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(Date()),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xB31A1A1A),
                modifier = Modifier.padding(horizontal = 22.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 简化日历：显示本月有记录的日期点
            MonthCalendarView(records = allRecords)

            Spacer(modifier = Modifier.height(16.dp))

            // 照片记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allRecords) { record ->
                    RecordCard(record = record)
                }

                if (allRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "还没有拍摄记录\n去拍摄你的第一张自然照片吧 🌱",
                                fontSize = 14.sp,
                                color = Color(0x991A1A1A),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 简化月历视图 — 标记有记录的日期
 */
@Composable
private fun MonthCalendarView(records: List<CaptureRecord>) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // 找出本月有记录的日期
    val daysWithRecords = records
        .filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
        .map {
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
        }
        .toSet()

    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 本月第一天是周几（周一=1）
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 转为周一起始

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 星期头
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0x661A1A1A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 构建日历格子数据：先填空位，再填日期
            val cells = mutableListOf<Int>() // 0=空白, >0=日期
            repeat(firstDayOfWeek) { cells.add(0) }
            for (d in 1..daysInMonth) { cells.add(d) }
            // 补足最后一行到7的倍数
            while (cells.size % 7 != 0) { cells.add(0) }

            // 按行渲染
            cells.chunked(7).forEach { weekRow ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekRow.forEach { cellDay ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        cellDay == today -> Color(0xFFE3F5F5)
                                        cellDay > 0 && cellDay in daysWithRecords -> Color(0x557DD4A8)
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellDay > 0) {
                                Text(
                                    text = "$cellDay",
                                    fontSize = 12.sp,
                                    fontWeight = if (cellDay == today) FontWeight.Bold else FontWeight.Normal,
                                    color = if (cellDay in daysWithRecords) Color(0xFF2D6E3E) else Color(0x801A1A1A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单条记录卡片
 */
@Composable
private fun RecordCard(record: CaptureRecord) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // 照片预览
            val photoFile = File(record.photoPath)
            if (photoFile.exists()) {
                AsyncImage(
                    model = photoFile,
                    contentDescription = "拍摄照片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            }

            // 分数和要素信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 光合值
                Text(
                    text = "+${record.photosynthesisValue}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LightGreen
                )

                // 四要素小标签
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElementBadge("☀️", record.lightScore, ElementLight)
                    ElementBadge("💧", record.waterScore, ElementWater)
                    ElementBadge("🌤️", record.airScore, ElementAir)
                    ElementBadge("🌳", record.biomeScore, ElementBiome)
                }
            }

            // 时间
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp)),
                fontSize = 11.sp,
                color = Color(0x991A1A1A),
                modifier = Modifier.padding(start = 12.dp, bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun ElementBadge(emoji: String, score: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 12.sp)
        Text(
            text = "$score",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}