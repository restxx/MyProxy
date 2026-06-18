package com.example.myproxy.network

import com.example.myproxy.data.ProxyNode
import com.example.myproxy.parser.VlessParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Base64
import java.util.concurrent.TimeUnit

class SubscriptionRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val SUBSCRIPTION_URL =
            "https://anvamu.ccwu.cc/sub?token=08614ceb8a1f1f257358ac3314589454"
    }

    suspend fun fetchNodes(): List<ProxyNode> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(SUBSCRIPTION_URL)
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Subscription request failed: ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val normalizedBody = normalizeSubscriptionBody(body)
            normalizedBody.lineSequence()
                .mapNotNull { VlessParser.parseLine(it) }
                .toList()
        }
    }

    private fun normalizeSubscriptionBody(rawBody: String): String {
        val trimmed = rawBody.trim()
        if (trimmed.contains("://")) return rawBody

        val decoded = runCatching {
            val bytes = Base64.getMimeDecoder().decode(trimmed)
            String(bytes, Charsets.UTF_8)
        }.getOrDefault("")

        return if (decoded.contains("://")) decoded else rawBody
    }
}
