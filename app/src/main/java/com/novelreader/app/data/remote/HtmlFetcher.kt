package com.novelreader.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HtmlFetcher @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    
    /**
     * 获取网页 HTML
     */
    suspend fun fetchHtml(url: String): Result<String> = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android 11; Mobile; rv:68.0) Gecko/68.0 Firefox/68.0")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en;q=0.3")
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            response.body?.string() ?: throw Exception("Empty response")
        }
    }
    
    /**
     * 获取并解析为 Document
     */
    suspend fun fetchDocument(url: String): Result<Document> = runCatching {
        val html = fetchHtml(url).getOrThrow()
        Jsoup.parse(html, url)
    }
    
    /**
     * 获取纯 JSON 内容（用于书源导入）
     */
    suspend fun fetchJson(url: String): Result<String> = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android 11; Mobile; rv:68.0) Gecko/68.0 Firefox/68.0")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en;q=0.3")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            response.body?.string() ?: throw Exception("Empty response")
        }
    }

    /**
     * 清理正文内容（去除广告、脚本等）
     */
    fun cleanContent(document: Document): String {
        // 移除脚本、样式等无关元素
        document.select("script, style, iframe, nav, header, footer, .ad, .advertisement, .comment").remove()
        return document.body()?.html() ?: ""
    }
}
