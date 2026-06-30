package com.novelreader.app.domain.model

import com.google.gson.annotations.SerializedName

/**
 * 书源模型 - 完整解析规则
 */
data class BookSource(
    @SerializedName("bookSourceName")
    val name: String = "",
    
    @SerializedName("bookSourceUrl")
    val baseUrl: String = "",
    
    @SerializedName("bookSourceGroup")
    val group: String = "",
    
    @SerializedName("bookSourceType")
    val type: String = "novel",
    
    @SerializedName("ruleSearchUrl")
    val searchUrl: String = "",
    
    @SerializedName("ruleSearchName")
    val searchNameRule: String = "",
    
    @SerializedName("ruleSearchAuthor")
    val searchAuthorRule: String = "",
    
    @SerializedName("ruleSearchDetailUrl")
    val searchDetailUrlRule: String = "",
    
    @SerializedName("ruleBookName")
    val bookNameRule: String = "",
    
    @SerializedName("ruleBookAuthor")
    val bookAuthorRule: String = "",
    
    @SerializedName("ruleCoverUrl")
    val coverUrlRule: String = "",
    
    @SerializedName("ruleBookIntro")
    val introRule: String = "",
    
    @SerializedName("ruleChapterUrl")
    val chapterUrlRule: String = "",
    
    @SerializedName("ruleChapterList")
    val chapterListRule: String = "",
    
    @SerializedName("ruleChapterName")
    val chapterNameRule: String = "",
    
    @SerializedName("ruleContent")
    val contentRule: String = "",
    
    @SerializedName("enabled")
    val enabled: Boolean = true,
    
    // 本地状态
    val id: Long = 0,
    val customOrder: Int = 0
) {
    companion object {
        fun fromJson(json: String): BookSource? {
            return try {
                com.google.gson.Gson().fromJson(json, BookSource::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toJson(): String = com.google.gson.Gson().toJson(this)
}
