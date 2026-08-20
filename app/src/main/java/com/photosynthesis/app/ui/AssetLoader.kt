package com.photosynthesis.app.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest

/**
 * 素材加载工具：从 assets 目录读取 PNG/SVG/Video
 *
 * 目录结构：
 *   assets/images/  → PNG 图片（启动页背景、植物卡片等）
 *   assets/svg/     → SVG 矢量图（图标、对话框、进度条等）
 *   assets/video/   → MP4 视频（植物动画）
 */
object AssetLoader {

    /** 创建支持 SVG 解码的 ImageLoader（全局单例） */
    fun svgImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    /** 从 assets/images/ 加载 PNG 为 ImageBitmap */
    fun loadBitmap(context: Context, fileName: String): ImageBitmap? {
        return try {
            context.assets.open("images/$fileName").use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    /** 构建 Coil ImageRequest（支持 SVG），用于 AsyncImage */
    fun svgRequest(context: Context, fileName: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data("file:///android_asset/svg/$fileName")
            .build()
    }

    /** 构建 Coil ImageRequest（PNG from assets/images） */
    fun imageRequest(context: Context, fileName: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data("file:///android_asset/images/$fileName")
            .build()
    }
}

/**
 * Composable 便捷方法：获取 SVG ImageLoader
 */
@Composable
fun rememberSvgImageLoader(): ImageLoader {
    val context = LocalContext.current
    return AssetLoader.svgImageLoader(context)
}
