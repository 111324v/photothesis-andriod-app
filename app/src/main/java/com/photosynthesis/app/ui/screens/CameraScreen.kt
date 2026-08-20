package com.photosynthesis.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.data.PhotoAnalyzer
import com.photosynthesis.app.data.PlantGrowthEngine
import com.photosynthesis.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 拍摄页
 * 对应原型 screen-camera：相机取景 + 扫描动画 + 四要素结果
 * 实现：调用系统相机拍照 → 发送给大模型API分析 → 显示结果
 */

// 拍摄流程状态
enum class CameraState {
    IDLE,       // 等待拍摄
    ANALYZING,  // AI分析中
    RESULT      // 显示结果
}

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cameraState by remember { mutableStateOf(CameraState.IDLE) }
    var analysisResult by remember { mutableStateOf<PhotoAnalyzer.AnalysisResult?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 创建临时文件用于存储拍摄的照片
    val tempPhotoFile = remember {
        createTempPhotoFile(context)
    }
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempPhotoFile
        )
    }

    // 系统相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoFile = tempPhotoFile
            // 开始AI分析
            cameraState = CameraState.ANALYZING
            scope.launch {
                analyzeAndSave(context, tempPhotoFile) { result ->
                    when {
                        result.isSuccess -> {
                            analysisResult = result.getOrNull()
                            cameraState = CameraState.RESULT
                        }
                        result.isFailure -> {
                            errorMessage = result.exceptionOrNull()?.message ?: "分析失败"
                            cameraState = CameraState.IDLE
                        }
                    }
                }
            }
        }
    }

    // 相机权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        } else {
            errorMessage = "需要相机权限才能拍照"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
            Text(
                text = "Capture Nature",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            // 图库入口
            Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = "图库",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateToArchive() }
            )
        }

        // 中间取景区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 160.dp),
            contentAlignment = Alignment.Center
        ) {
            when (cameraState) {
                CameraState.IDLE -> {
                    // 待拍摄提示
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "将镜头对准任意自然场景",
                            fontSize = 15.sp,
                            color = Color(0x8CFFFFFF),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sun · Water · Sky · Nature",
                            fontSize = 13.sp,
                            color = Color(0x61FFFFFF)
                        )
                    }
                }

                CameraState.ANALYZING -> {
                    // 分析中动画
                    AnalyzingAnimation()
                }

                CameraState.RESULT -> {
                    // 分析结果展示
                    analysisResult?.let { result ->
                        ResultDisplay(result = result)
                    }
                }
            }
        }

        // 错误提示
        errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(msg)
            }
            // 3秒后清除错误
            LaunchedEffect(msg) {
                delay(3000)
                errorMessage = null
            }
        }

        // 底部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图库按钮
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable { onNavigateToArchive() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhotoLibrary, "图库", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // 快门按钮
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        if (cameraState == CameraState.RESULT) Color(0xFFFFD97D)
                        else Color.White
                    )
                    .clickable(enabled = cameraState != CameraState.ANALYZING) {
                        if (cameraState == CameraState.RESULT) {
                            // 结果状态下再次点击：重新拍摄
                            cameraState = CameraState.IDLE
                            analysisResult = null
                        }
                        // 请求相机权限并拍照
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (cameraState == CameraState.RESULT) Color(0xFFFFD97D)
                            else Color(0xFFF5F5F5)
                        )
                )
            }

            // 占位（保持布局对称）
            Spacer(modifier = Modifier.size(46.dp))
        }
    }
}

/**
 * AI 分析中动画组件
 */
@Composable
private fun AnalyzingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "analyzing")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 四要素脉冲动画
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            val elements = listOf("☀️" to "Sun", "💧" to "Water", "🌤️" to "Sky", "🌳" to "Nature")
            elements.forEachIndexed { index, (emoji, name) ->
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 400)
                    ),
                    label = "elem$index"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 19.sp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = name, fontSize = 11.sp, color = Color(0x6BFFFFFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("AI Analyzing Elements", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xD1FFFFFF))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Sun · Water · Sky · Nature...", fontSize = 13.sp, color = Color(0x61FFFFFF))
    }
}

/**
 * 分析结果展示组件
 */
@Composable
private fun ResultDisplay(result: PhotoAnalyzer.AnalysisResult) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 四要素评分卡片
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x2EFFFFFF))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ElementScoreItem("☀️", "Sun", result.lightScore, ElementLight)
                ElementScoreItem("💧", "Water", result.waterScore, ElementWater)
                ElementScoreItem("🌤️", "Sky", result.airScore, ElementAir)
                ElementScoreItem("🌳", "Nature", result.biomeScore, ElementBiome)
            }
        }

        // 总光合值
        Text(
            text = "+${result.totalScore} Energy",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LightGreen
        )
    }
}

@Composable
private fun ElementScoreItem(emoji: String, name: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$score", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 11.sp, color = Color(0x99FFFFFF))
    }
}

/**
 * 创建临时照片文件
 */
private fun createTempPhotoFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.filesDir, "photos").apply { mkdirs() }
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

/**
 * 调用大模型API分析照片并保存结果到数据库
 */
private suspend fun analyzeAndSave(
    context: Context,
    photoFile: File,
    onResult: (Result<PhotoAnalyzer.AnalysisResult>) -> Unit
) {
    // TODO: 用户需要在设置中配置 API Key
    // 这里使用 SharedPreferences 读取
    val prefs = context.getSharedPreferences("photosynthesis_config", Context.MODE_PRIVATE)
    val apiKey = prefs.getString("api_key", "") ?: ""
    val apiUrl = prefs.getString("api_url", "https://api.openai.com/v1/chat/completions") ?: ""
    val model = prefs.getString("model", "gpt-4o-mini") ?: ""

    if (apiKey.isEmpty()) {
        onResult(Result.failure(Exception("请先在设置中配置 API Key")))
        return
    }

    val analyzer = PhotoAnalyzer(apiKey, apiUrl, model)
    val result = analyzer.analyzePhoto(photoFile)

    if (result.isSuccess) {
        val analysisResult = result.getOrNull()!!
        // 保存到数据库
        val db = PhotosynthesisApp.instance.database
        val record = CaptureRecord(
            photoPath = photoFile.absolutePath,
            timestamp = System.currentTimeMillis(),
            lightScore = analysisResult.lightScore,
            waterScore = analysisResult.waterScore,
            airScore = analysisResult.airScore,
            biomeScore = analysisResult.biomeScore,
            photosynthesisValue = analysisResult.totalScore
        )
        db.captureRecordDao().insert(record)

        // 更新植物生长状态：获取最近7天记录计算总值
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
        val recentRecords = db.captureRecordDao().getRecentRecords(sevenDaysAgo)
        val allTotal = recentRecords.sumOf { it.photosynthesisValue }
        val stage = PlantGrowthEngine.calculateStage(allTotal)
        db.plantStateDao().updateGrowth(allTotal, stage.name.lowercase())
    }

    onResult(result)
}