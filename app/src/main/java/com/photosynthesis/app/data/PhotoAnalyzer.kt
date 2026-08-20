package com.photosynthesis.app.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 照片分析器
 * 将拍摄的照片发送给大模型 API，获取四要素评分
 */
class PhotoAnalyzer(
    private val apiKey: String,
    private val apiUrl: String = "https://api.openai.com/v1/chat/completions",
    private val model: String = "gpt-4o-mini"
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * 分析结果数据类
     */
    data class AnalysisResult(
        val lightScore: Int,  // 光 0-3
        val waterScore: Int,  // 水 0-3
        val airScore: Int,    // 气 0-3
        val biomeScore: Int   // 境 0-3
    ) {
        val totalScore: Int get() = lightScore + waterScore + airScore + biomeScore
    }

    /**
     * 分析照片，返回四要素评分
     * @param photoFile 照片文件
     * @return 四要素评分结果
     */
    suspend fun analyzePhoto(photoFile: File): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 将图片转为 Base64
            val imageBytes = photoFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val mimeType = "image/jpeg"

            // 构建请求 JSON
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:$mimeType;base64,$base64Image")
                                put("detail", "low") // 低精度足够，节省token
                            })
                        })
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", USER_PROMPT)
                        })
                    })
                })
            }

            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messagesArray)
                put("max_tokens", 100)
                put("temperature", 0.3) // 低温度确保稳定输出
            }

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("空响应")

            if (!response.isSuccessful) {
                throw Exception("API请求失败: ${response.code} - $responseBody")
            }

            // 解析响应
            val jsonResponse = JSONObject(responseBody)
            val content = jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // 解析大模型返回的 JSON 格式评分
            val scores = parseScores(content)
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 解析大模型返回的评分文本
     * 期望格式：{"light":2,"water":1,"air":3,"biome":2}
     */
    private fun parseScores(content: String): AnalysisResult {
        return try {
            // 尝试直接解析 JSON
            val json = JSONObject(content.replace(Regex("[^{}\\d,:\"a-z]"), ""))
            AnalysisResult(
                lightScore = json.optInt("light", 1).coerceIn(0, 3),
                waterScore = json.optInt("water", 0).coerceIn(0, 3),
                airScore = json.optInt("air", 1).coerceIn(0, 3),
                biomeScore = json.optInt("biome", 0).coerceIn(0, 3)
            )
        } catch (e: Exception) {
            // JSON解析失败时，尝试从文本中提取数字
            val numbers = Regex("\\d").findAll(content).map { it.value.toInt() }.toList()
            if (numbers.size >= 4) {
                AnalysisResult(
                    lightScore = numbers[0].coerceIn(0, 3),
                    waterScore = numbers[1].coerceIn(0, 3),
                    airScore = numbers[2].coerceIn(0, 3),
                    biomeScore = numbers[3].coerceIn(0, 3)
                )
            } else {
                // 兜底：给一个最低分
                AnalysisResult(1, 0, 1, 0)
            }
        }
    }

    companion object {
        // 系统提示词：告诉大模型它的角色和评分规则
        private const val SYSTEM_PROMPT = """你是"光合作用"App的自然场景分析引擎。
用户会发送一张照片，你需要从中识别四种自然要素并打分。

评分规则（每项0-3分整数）：
- light（光）：画面亮度/阳光程度。0=黑暗室内 1=微弱光线 2=明亮散射光 3=直射阳光
- water（水）：水元素存在感。0=无水 1=湿润/水汽 2=明显水体/雨 3=大面积水域
- air（气）：天空/空气开阔感。0=密闭空间 1=窗户可见天 2=户外可见天空 3=广阔天空
- biome（境）：植被/自然环境丰富度。0=纯人工环境 1=少量绿植 2=公园级绿化 3=自然森林

你必须且只能返回一个JSON对象，格式：{"light":X,"water":X,"air":X,"biome":X}
不要返回任何其他文字。"""

        // 用户提示词
        private const val USER_PROMPT = "请分析这张照片的四要素评分，只返回JSON。"
    }
}
