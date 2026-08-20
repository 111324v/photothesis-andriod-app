package com.photosynthesis.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.photosynthesis.app.ui.AssetLoader
import com.photosynthesis.app.ui.rememberSvgImageLoader

/**
 * 启动页 - 还原设计稿
 * 背景：启动页-背景.png（全屏铺满）
 * 中央：启动页-icon.svg + 标题 + 副标题
 * 底部：呼吸动画提示文字 "Tap to Start"
 */
@Composable
fun SplashScreen(onTap: () -> Unit) {
    val context = LocalContext.current
    val svgLoader = rememberSvgImageLoader()

    // 呼吸动画：底部提示文字明暗闪烁
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() }
    ) {
        // 全屏背景图
        AsyncImage(
            model = AssetLoader.imageRequest(context, "\u542f\u52a8\u9875-\u80cc\u666f.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 中央内容：icon + 标题 + 副标题
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo（SVG）
            AsyncImage(
                model = AssetLoader.svgRequest(context, "\u542f\u52a8\u9875-icon.svg"),
                contentDescription = "Photosynthesis",
                imageLoader = svgLoader,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 主标题
            Text(
                text = "Photosynthesis",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题
            Text(
                text = "Grow every bit of nature you see\ninto your plant companion.",
                fontSize = 14.sp,
                color = Color(0xFF074066),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // 底部呼吸提示
        Text(
            text = "Tap to Start",
            fontSize = 13.sp,
            color = Color(0xFF074066).copy(alpha = alpha),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}
