package com.example.service

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
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Dedicated Gemini REST API client supporting system instructions,
 * structured JSON output, and automatic model fallback.
 */
class GeminiClient {

  private val tag = "GeminiClient"

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  // Primary model and fallback candidate list
  private val candidateModels = listOf(
    "gemini-3.5-flash",
    "gemini-3.1-flash-lite-preview",
    "gemini-flash-latest"
  )

  fun getApiKey(): String {
    val key = BuildConfig.GEMINI_API_KEY
    if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") {
      return key
    }

    val envBuildKey = BuildConfig.GEMINI_API_KEY_ENV
    if (envBuildKey.isNotBlank() && envBuildKey != "MY_GEMINI_API_KEY") {
      return envBuildKey
    }

    val systemKey = System.getenv("GEMINI_API_KEY")
    if (!systemKey.isNullOrBlank() && systemKey != "MY_GEMINI_API_KEY") {
      return systemKey
    }

    return ""
  }

  suspend fun generateContent(
    systemInstruction: String,
    userPrompt: String,
    jsonMode: Boolean = false
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException("GEMINI_API_KEY is not configured. Please set your key in the AI Studio Secrets panel.")
      )
    }

    var lastException: Exception? = null

    for (model in candidateModels) {
      try {
        val result = executeRequest(model, apiKey, systemInstruction, userPrompt, jsonMode)
        if (result.isSuccess) {
          return@withContext result
        } else {
          lastException = result.exceptionOrNull() as? Exception
          Log.w(tag, "Model $model failed: ${lastException?.message}, trying fallback...")
        }
      } catch (e: Exception) {
        lastException = e
        Log.w(tag, "Model $model encountered error: ${e.message}, trying fallback...")
      }
    }

    Result.failure(lastException ?: IOException("Failed to generate content across available models."))
  }

  private fun executeRequest(
    model: String,
    apiKey: String,
    systemInstruction: String,
    userPrompt: String,
    jsonMode: Boolean
  ): Result<String> {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    val requestJson = JSONObject().apply {
      // System Instruction
      if (systemInstruction.isNotBlank()) {
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", systemInstruction) })
          })
        })
      }

      // Contents
      put("contents", JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", userPrompt) })
          })
        })
      })

      // Generation Config
      val config = JSONObject().apply {
        put("temperature", 0.2)
        if (jsonMode) {
          put("responseMimeType", "application/json")
        }
      }
      put("generationConfig", config)
    }

    val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(url)
      .post(body)
      .build()

    return try {
      val response = okHttpClient.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        val errorMsg = try {
          val errorJson = JSONObject(responseBody).optJSONObject("error")
          errorJson?.optString("message") ?: "HTTP ${response.code}: $responseBody"
        } catch (_: Exception) {
          "HTTP ${response.code}: $responseBody"
        }
        return Result.failure(IOException("Gemini API error ($model): $errorMsg"))
      }

      val json = JSONObject(responseBody)
      val candidates = json.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        val promptFeedback = json.optJSONObject("promptFeedback")
        val blockReason = promptFeedback?.optString("blockReason")
        return Result.failure(IOException("Gemini returned 0 candidates ($model). BlockReason: ${blockReason ?: "NONE"}"))
      }

      val firstCandidate = candidates.optJSONObject(0)
      val finishReason = firstCandidate?.optString("finishReason")
      val content = firstCandidate?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")

      val textBuilder = StringBuilder()
      if (parts != null) {
        for (i in 0 until parts.length()) {
          val p = parts.optJSONObject(i)
          val t = p?.optString("text")
          if (!t.isNullOrEmpty()) {
            textBuilder.append(t)
          }
        }
      }
      val text = textBuilder.toString()

      if (text.isNotBlank()) {
        Result.success(text)
      } else {
        Result.failure(IOException("Empty response text from model $model. finishReason: $finishReason"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
