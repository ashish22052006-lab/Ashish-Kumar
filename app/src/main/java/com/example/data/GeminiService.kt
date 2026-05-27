package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Configured with 60s timeouts as requested by gemini-api guidelines
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val API_MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$API_MODEL:generateContent"

    suspend fun generateResponse(prompt: String, systemInstruction: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please configure your API key in the Secrets panel in AI Studio UI."
        }

        try {
            val url = "$BASE_URL?key=$apiKey"
            
            // Build the JSON request body manually for absolute control and safety
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction if provided
            if (systemInstruction.isNotEmpty()) {
                val sysInstructionObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstructionObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstructionObj)
            }

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed code: ${response.code}, body: $errBody")
                    return@withContext "Failed to connect to AI Mentor. Error code: ${response.code}. Please ensure your API key is correctly entered."
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext "AI Mentor returned an empty response."
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No response text.")
                        }
                    }
                }
                return@withContext "No response. Ensure your prompt details are clean."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            return@withContext "Offline Mode: Connection to the AI Mentor failed (${e.localizedMessage ?: "Network issue"}). Rest assured, our smart offline heuristics will continue guiding you!"
        }
    }

    // High Level Features using Gemini
    suspend fun getAIStudyRecommendations(
        syllabusSummary: String,
        studySessionSummary: String,
        mockTestPerformance: String,
        targetCollege: String,
        targetRank: String
    ): String {
        val systemPrompt = "You are an elite IIT JEE Mentor (with years of experience guiding top 100 AIRs). " +
                "Analyze the candidate's syllabus completion, daily study session logs, weak subjects, and mock test scores, " +
                "and give them a highly motivational, extremely strategic study plan, actionable steps to master weak topics, and daily routine adjustments. " +
                "Be encouraging, highly structured, and mention iconic textbooks where relevant (e.g., Irodov, HC Verma, JD Lee, MS Chouhan, RD Sharma, Cengage)."

        val userPrompt = """
            My Target College: $targetCollege
            My Target Rank: $targetRank (JEE)
            
            Syllabus Completion & Weak Areas:
            $syllabusSummary
            
            Daily Study Session Logs:
            $studySessionSummary
            
            Mock Test Performance:
            $mockTestPerformance
            
            Please provide:
            1. Strategy to improve weak areas (identifying which subjects/topics need urgent focus based on mock study balance).
            2. Recommendations on how to raise mock scores.
            3. Detailed optimal study schedule/revision timetable (balanced for coaching vs self-study).
            4. Specific topics to prioritize for immediate revision using elite books.
            5. JEE Motivation boost.
        """.trimIndent()

        return generateResponse(userPrompt, systemPrompt)
    }

    suspend fun getMockTestPercentileAndRankPrediction(
        physicsScore: Int,
        chemistryScore: Int,
        mathsScore: Int,
        totalScore: Int,
        maxScore: Int
    ): Pair<Double, Int> {
        // Standard high-quality predictive fallback logic if internet fails, but if we can,
        // we can estimate using JEE percentile distributions!
        // Total score standard ranges (for JEE Main, full mark 300):
        // 250+ -> 99.9+ Percentile, Rank 1 - 1000
        // 200 - 249 -> 99.5 - 99.8 Percentile, Rank 1000 - 5000
        // 170 - 199 -> 99.0 - 99.4 Percentile, Rank 5000 - 10000
        // 140 - 169 -> 97.0 - 98.9 Percentile, Rank 10000 - 25000
        // 110 - 139 -> 94.0 - 96.9 Percentile, Rank 25000 - 50000
        // 80 - 109 -> 90.0 - 93.9 Percentile, Rank 50000 - 100000
        // <80 -> <90 Percentile, Rank > 100000
        
        // Let's do a reliable dynamic calculation
        val scoreRatio = totalScore.toDouble() / maxScore.toDouble()
        val percentile = when {
            scoreRatio >= 0.85 -> 99.9 - (0.85 - scoreRatio) * 0.1
            scoreRatio >= 0.70 -> 99.5 + ((scoreRatio - 0.70) / 0.15) * 0.4
            scoreRatio >= 0.55 -> 98.5 + ((scoreRatio - 0.55) / 0.15) * 1.0
            scoreRatio >= 0.45 -> 96.0 + ((scoreRatio - 0.45) / 0.10) * 2.5
            scoreRatio >= 0.35 -> 91.0 + ((scoreRatio - 0.35) / 0.10) * 5.0
            scoreRatio >= 0.25 -> 80.0 + ((scoreRatio - 0.25) / 0.10) * 11.0
            else -> max(10.0, scoreRatio * 320.0)
        }
        
        val predictedRank = when {
            percentile >= 99.9 -> max(1, (1000 - (percentile - 99.9) * 10000).toInt())
            percentile >= 99.5 -> (1000 + (99.9 - percentile) * 10000).toInt()
            percentile >= 99.0 -> (5000 + (99.5 - percentile) * 10000).toInt()
            percentile >= 97.0 -> (10000 + (99.0 - percentile) * 7500).toInt()
            percentile >= 95.0 -> (25000 + (97.0 - percentile) * 12500).toInt()
            percentile >= 90.0 -> (50000 + (95.0 - percentile) * 10000).toInt()
            else -> (100000 + (90.0 - percentile) * 15000).toInt()
        }

        return Pair(percentile, predictedRank)
    }
}
