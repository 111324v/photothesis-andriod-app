package com.photosynthesis.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.photosynthesis.app.ui.AssetLoader
import com.photosynthesis.app.ui.rememberSvgImageLoader

/**
 * 主页面 - 还原设计稿
 * - 视频背景（幼苗缓动）
 * - 天空渐变兜底
 * - 植物名称 + 状态
 * - 独白对话框（SVG气泡）
 * - FAB 圆盘菜单（拍摄/图库）
 */
@Composable
fun HomeScreen(
    onNavigateCamera: () -> Unit,
    onNavigateArchive: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val svgLoader = rememberSvgImageLoader()

    // FAB 展开状态
    var fabExpanded by remember { mutableStateOf(false) }

    // 微风呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "breeze")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plant_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 天空渐变兜底层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7DD8EE),  // 天空蓝
                            Color(0xFF9EE0D0),  // 过渡青
                            Color(0xFF6AB84A)   // 底部绿
                        )
                    )
                )
        )

        // 视频背景（植物动画）
        VideoBackground(
            context = context,
            videoFileName = "\u5e7c\u82d7-\u7f13\u52a8.mp4",  // 幼苗-缓动.mp4
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
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
                    text = "Sunflower",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0C3318)
                )
                Text(
                    text = "RADIANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A4A22),
                    letterSpacing = 5.sp
                )
            }

            // 右上角"我的"按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.4f))
                    .clickable { onNavigateProfile() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = AssetLoader.svgRequest(context, "\u6211\u7684-icon.svg"),
                    contentDescription = "Profile",
                    imageLoader = svgLoader,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 独白对话框
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 280.dp, end = 10.dp)
                .width(311.dp)
                .height(123.dp)
        ) {
            // SVG 气泡背景
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u5bf9\u8bdd\u6846.svg"),
                contentDescription = null,
                imageLoader = svgLoader,
                modifier = Modifier.fillMaxSize()
            )
            // 独白文字
            Text(
                text = "Every bit of sunlight this week \u2014 I received it all \u2728",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF074066),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 27.dp, start = 34.dp, end = 29.dp)
                    .width(248.dp)
            )
        }

        // FAB 系统
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 48.dp)
        ) {
            // 展开时的圆盘背景
            if (fabExpanded) {
                Box(
                    modifier = Modifier
                        .size(287.dp)
                        .offset(x = (-96).dp, y = (-96).dp)
                        .clip(CircleShape)
                        .background(Color(0xC4060F0A))
                ) {
                    // 拍摄按钮（左侧9点方向）
                    Box(
                        modifier = Modifier
                            .offset(x = 21.dp, y = 121.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.11f))
                            .clickable {
                                fabExpanded = false
                                onNavigateCamera()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\uD83D\uDCF7", fontSize = 18.sp)
                    }
                    Text(
                        "Camera",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.offset(x = 21.dp, y = 173.dp).width(44.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // 图库按钮（正上12点方向）
                    Box(
                        modifier = Modifier
                            .offset(x = 121.dp, y = 21.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.11f))
                            .clickable {
                                fabExpanded = false
                                onNavigateArchive()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\uD83D\uDDBC", fontSize = 18.sp)
                    }
                    Text(
                        "Archive",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.offset(x = 121.dp, y = 73.dp).width(44.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // 主 FAB 按钮
            AsyncImage(
                model = AssetLoader.svgRequest(
                    context,
                    if (fabExpanded) "\u53f3\u4e0b\u89d2-\u60ac\u6d6eicon-\u5c55\u5f00\u6001.svg"
                    else "\u53f3\u4e0b\u89d2-\u60ac\u6d6eicon-\u6536\u8d77\u72b6\u6001.svg"
                ),
                contentDescription = "Menu",
                imageLoader = svgLoader,
                modifier = Modifier
                    .size(96.dp)
                    .clickable { fabExpanded = !fabExpanded }
                    .graphicsLayer(rotationZ = if (fabExpanded) 45f else 0f)
            )
        }

        // 展开遮罩（点击关闭FAB）
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { fabExpanded = false }
            )
        }
    }
}

/**
 * 视频背景播放组件
 * 使用 ExoPlayer 播放 assets/video/ 目录下的 MP4
 */
@Composable
fun VideoBackground(
    context: Context,
    videoFileName: String,
    modifier: Modifier = Modifier
) {
    // 创建并记住 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("file:///android_asset/video/$videoFileName")
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL  // 循环播放
            playWhenReady = true
            volume = 0f  // 静音
            prepare()
        }
    }

    // 页面销毁时释放播放器
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // 渲染 PlayerView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false  // 隐藏控制栏
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                // 设置背景透明，让渐变层透过
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = modifier
    )
}
