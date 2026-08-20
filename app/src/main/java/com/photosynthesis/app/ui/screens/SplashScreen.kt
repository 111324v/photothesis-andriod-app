package com.photosynthesis.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 启动页
 * 对应原型 screen-splash：深色渐变背景 + 品牌标题 + 点击进入
 */
@Composable
fun SplashScreen(onTap: () -> Unit) {
    // 呼吸动画：底部提示文字闪烁
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2818), // 深墨绿
                        Color(0xFF060E08)  // 近黑色
                    )
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 应用图标占位（用emoji代替，后续可换png）
            Text(
                text = "🌱",
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 英文品牌名
            Text(
                text = "Photosynthesis",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题
            Text(
                text = "Grow every bit of nature you see\ninto your plant companion.",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF7DD4A8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // 底部点击提示
        Text(
            text = "Tap to Start",
            fontSize = 13.sp,
            color = Color(0xFF7DD4A8),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(hintAlpha)
        )
    }
}
