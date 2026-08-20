package com.photosynthesis.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.photosynthesis.app.PhotosynthesisApp
import com.photosynthesis.app.data.CaptureRecord
import com.photosynthesis.app.data.PhotoAnalyzer
import com.photosynthesis.app.data.PlantGrowthEngine
import com.photosynthesis.app.ui.AssetLoader
import com.photosynthesis.app.ui.rememberSvgImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 拍摄页 - 还原设计稿
 * 流程：打开相机 → AI分析 → 展示四要素得分 → 保存记录
 */
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onNavigateArchive: () -> Unit
) {
    val context = LocalContext.current
    val svgLoader = rememberSvgImageLoader()
    val scope = rememberCoroutineScope()

    // 状态机：idle → capturing → analyzing → result
    var screenState by remember { mutableStateOf("idle") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }

    // 创建临时文件用于相机拍照
    val photoFile = remember {
        File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    }
    val photoUriForCamera = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }

    // 系统相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = photoUriForCamera
            screenState = "analyzing"
            // 调用 AI 分析
            scope.launch {
                val result = analyzePhoto(context, photoFile)
                analysisResult = result
                screenState = "result"
                // 保存到数据库
                saveRecord(context, photoFile.absolutePath, result)
            }
        } else {
            screenState = "idle"
        }
    }

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUriForCamera)
            screenState = "capturing"
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() }
                    .padding(8.dp)
            )
            Text(
                "Capture Nature",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp)
            )
        }

        // 主内容区域
        when (screenState) {
            "idle" -> {
                // 取景框 + 提示文字
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp, bottom = 160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\u5c06\u955c\u5934\u5bf9\u51c6\u4efb\u610f\u81ea\u7136\u573a\u666f",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sun \u00b7 Water \u00b7 Sky \u00b7 Nature",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.38f)
                    )
                }
            }
            "analyzing" -> {
                // AI 分析中动画
                AnalyzingOverlay(context, svgLoader)
            }
            "result" -> {
                // 结果展示
                analysisResult?.let { result ->
                    ResultOverlay(result, context, svgLoader)
                }
            }
        }

        // 底部控制栏
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(144.dp)
                .background(Color.Black),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 图库按钮
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u62cd\u6444\u9875-\u56fe\u5e93icon.svg"),
                contentDescription = "Gallery",
                imageLoader = svgLoader,
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onNavigateArchive() }
            )

            // 快门按钮
            AsyncImage(
                model = AssetLoader.svgRequest(
                    context,
                    if (screenState == "result") "\u62cd\u6444\u9875-\u62cd\u6444\u6309\u94ae-\u70b9\u51fb\u540e.svg"
                    else "\u62cd\u6444icon-\u767d\u8272.svg"
                ),
                contentDescription = "Capture",
                imageLoader = svgLoader,
                modifier = Modifier
                    .size(82.dp)
                    .clickable {
                        if (screenState == "idle" || screenState == "result") {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
            )

            // 翻转镜头按钮
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u62cd\u6444\u9875-\u65cb\u8f6c\u955c\u5934icon.svg"),
                contentDescription = "Flip",
                imageLoader = svgLoader,
                modifier = Modifier.size(46.dp)
            )
        }
    }
}

/**
 * AI 分析中的遮罩动画
 */
@Composable
private fun AnalyzingOverlay(context: Context, svgLoader: coil.ImageLoader) {
    val infiniteTransition = rememberInfiniteTransition(label = "analyze")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEB030A05)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 识别动画 SVG
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u62cd\u6444\u9875-\u8bc6\u522b\u4e2d\u52a8\u753b.svg"),
                contentDescription = null,
                imageLoader = svgLoader,
                modifier = Modifier.size(180.dp)
            )

            Text(
                "AI Analyzing Elements",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.82f)
            )
            Text(
                "Sun \u00b7 Water \u00b7 Sky \u00b7 Nature...",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.38f)
            )

            // 四要素脉冲圆
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                listOf("\u2600\ufe0f" to "Sun", "\ud83d\udca7" to "Water", "\ud83c\udf24\ufe0f" to "Sky", "\ud83c\udf33" to "Nature")
                    .forEachIndexed { index, (emoji, label) ->
                        val animAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 0.85f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1600, easing = EaseInOut, delayMillis = index * 400),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_$index"
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7DD4A8).copy(alpha = animAlpha * 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 19.sp)
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.42f))
                        }
                    }
            }
        }
    }
}

/**
 * 结果展示遮罩
 */
@Composable
private fun ResultOverlay(result: AnalysisResult, context: Context, svgLoader: coil.ImageLoader) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 160.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // 四要素得分面板
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ElementScore("sun.svg", "Sun", result.light, Color(0xFFFFD97D), context, svgLoader)
                    ElementScore("water.svg", "Water", result.water, Color(0xFF71D4F3), context, svgLoader)
                    ElementScore("sky.svg", "Sky", result.air, Color(0xFF74B9FF), context, svgLoader)
                    ElementScore("nature.svg", "Nature", result.biome, Color(0xFF7DD4A8), context, svgLoader)
                }
            }

            // 能量值
            AsyncImage(
                model = AssetLoader.svgRequest(context, "+8.6 Energy.svg"),
                contentDescription = "Energy gained",
                imageLoader = svgLoader,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

@Composable
private fun ElementScore(
    svgFile: String,
    label: String,
    score: Int,
    color: Color,
    context: Context,
    svgLoader: coil.ImageLoader
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$score",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        AsyncImage(
            model = AssetLoader.svgRequest(context, svgFile),
            contentDescription = label,
            imageLoader = svgLoader,
            modifier = Modifier.size(36.dp)
        )
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

/** 分析结果数据类 */
data class AnalysisResult(
    val light: Int,
    val water: Int,
    val air: Int,
    val biome: Int,
    val totalValue: Float
)

/** 调用 AI 分析照片 */
private suspend fun analyzePhoto(context: Context, photoFile: File): AnalysisResult {
    return withContext(Dispatchers.IO) {
        try {
            val scores = PhotoAnalyzer.analyze(context, photoFile.absolutePath)
            val totalValue = (scores.first + scores.second + scores.third + scores.fourth).toFloat() * 0.86f
            AnalysisResult(scores.first, scores.second, scores.third, scores.fourth, totalValue)
        } catch (e: Exception) {
            // 分析失败时返回默认值
            AnalysisResult(2, 2, 2, 2, 6.8f)
        }
    }
}

/** 保存拍照记录到数据库 */
private suspend fun saveRecord(context: Context, photoPath: String, result: AnalysisResult) {
    withContext(Dispatchers.IO) {
        val db = (context.applicationContext as PhotosynthesisApp).database
        val record = CaptureRecord(
            photoPath = photoPath,
            timestamp = System.currentTimeMillis(),
            lightScore = result.light,
            waterScore = result.water,
            airScore = result.air,
            biomeScore = result.biome,
            photosynthesisValue = result.totalValue
        )
        db.captureRecordDao().insert(record)

        // 更新植物生长状态
        val recentRecords = db.captureRecordDao().getRecentRecords(20)
        val newStage = PlantGrowthEngine.calculateStage(
            db.captureRecordDao().getTotalPhotosynthesis()
        )
        db.plantStateDao().updateGrowth(
            totalPhotosynthesis = db.captureRecordDao().getTotalPhotosynthesis(),
            growthStage = newStage.name
        )
    }
}
