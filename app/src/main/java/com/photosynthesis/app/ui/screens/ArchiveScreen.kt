package com.photosynthesis.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.ui.AssetLoader
import com.photosynthesis.app.ui.rememberSvgImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图库/Light Archive 页面 - 还原设计稿
 * 天蓝色背景 + 日历视图 + 照片记录卡片 + 四要素柱状图
 */
@Composable
fun ArchiveScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val svgLoader = rememberSvgImageLoader()
    val scrollState = rememberScrollState()

    // 加载记录
    var records by remember { mutableStateOf<List<CaptureRecord>>(emptyList()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = (context.applicationContext as PhotosynthesisApp).database
            records = db.captureRecordDao().getAllRecords()
        }
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH) }
    val today = remember { dateFormat.format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB))
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
                    .size(38.dp)
                    .clickable { onBack() }
                    .padding(8.dp)
            )
            // 筛选按钮占位
            Spacer(modifier = Modifier.size(38.dp))
        }

        // 标题
        Text(
            text = "Light Archive",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 22.dp)
        )

        // 日期导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = today,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A).copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 日历网格（简化：显示当月有记录的日期）
        CalendarGrid(records, context, svgLoader)

        Spacer(modifier = Modifier.height(16.dp))

        // 照片记录卡片
        if (records.isNotEmpty()) {
            // 图片群组 SVG
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u56fe\u7247\u7fa4\u7ec4.svg"),
                contentDescription = "Photo group",
                imageLoader = svgLoader,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 四要素进度条
        AsyncImage(
            model = AssetLoader.svgRequest(context, "\u56db\u8981\u7d20\u8fdb\u5ea6\u6761.svg"),
            contentDescription = "Element bars",
            imageLoader = svgLoader,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * 日历网格组件
 */
@Composable
private fun CalendarGrid(
    records: List<CaptureRecord>,
    context: android.content.Context,
    svgLoader: coil.ImageLoader
) {
    val calendar = remember { Calendar.getInstance() }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7  // Monday=0
    }

    // 有记录的日期集合
    val recordDays = remember(records) {
        val cal = Calendar.getInstance()
        records.map { record ->
            cal.timeInMillis = record.timestamp
            cal.get(Calendar.DAY_OF_MONTH)
        }.toSet()
    }

    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
        // 星期标题行
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 日期格子
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val hasRecord = day in recordDays
                            val isToday = day == today

                            if (hasRecord) {
                                // 有记录的日期用 SVG 背景
                                AsyncImage(
                                    model = AssetLoader.svgRequest(
                                        context,
                                        if (isToday) "\u65e5\u5386-\u9009\u4e2d\u6001.svg"
                                        else "\u65e5\u5386-\u6709\u8bb0\u5f55-\u672a\u9009\u4e2d\u6001.svg"
                                    ),
                                    contentDescription = null,
                                    imageLoader = svgLoader,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (isToday || day % 5 == 0) {
                                Text(
                                    text = "$day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
