package com.novelreader.app.domain.model

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 书源模型 - 同时支持 Shuyuan (snake_case) 和书源校验文件 (camelCase) 格式
 */
data class BookSource(
    val name: String = "",
    val baseUrl: String = "",
    val group: String = "",
    val type: String = "novel",
    val searchUrl: String = "",
    val searchNameRule: String = "",
    val searchAuthorRule: String = "",
    val searchDetailUrlRule: String = "",
    val bookNameRule: String = "",
    val bookAuthorRule: String = "",
    val coverUrlRule: String = "",
    val introRule: String = "",
    val chapterUrlRule: String = "",
    val chapterListRule: String = "",
    val chapterNameRule: String = "",
    val contentRule: String = "",
    val enabled: Boolean = true,
    // 本地状态（不参与 JSON 反序列化）
    val id: Long = 0,
    val customOrder: Int = 0
) {
    companion object {
        /** 通用的 Gson 实例，自动兼容 snake_case / camelCase */
        val gson: Gson by lazy {
            GsonBuilder()
                .registerTypeAdapter(BookSource::class.java, BookSourceDeserializer())
                .registerTypeAdapter(object : TypeToken<List<BookSource>>() {}.type, BookSourceListDeserializer())
                .create()
        }

        fun fromJson(json: String): BookSource? {
            return try {
                gson.fromJson(json.trim(), BookSource::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun fromJsonList(json: String): List<BookSource> {
            return try {
                gson.fromJson(json.trim(), object : TypeToken<List<BookSource>>() {}.type) ?: emptyList()
            } catch (e: Exception) {
                // 尝试单对象兜底
                val single = fromJson(json)
                if (single != null && single.name.isNotBlank()) listOf(single) else emptyList()
            }
        }
    }

    fun toJson(): String = gson.toJson(this)
}

/** 单个书源的反序列化器：同时支持 snake_case 和 camelCase */
class BookSourceDeserializer : JsonDeserializer<BookSource> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): BookSource? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject

        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""

        return BookSource(
            name = obj["book_source_name"].str() ?: obj["bookSourceName"].str(),
            baseUrl = obj["book_source_url"].str() ?: obj["bookSourceUrl"].str(),
            group = obj["book_source_group"].str() ?: obj["bookSourceGroup"].str(),
            type = obj["book_source_type"].str() ?: obj["bookSourceType"].str() ?: "novel",
            searchUrl = obj["rule_search_url"].str() ?: obj["ruleSearchUrl"].str(),
            searchNameRule = obj["rule_search_name"].str() ?: obj["ruleSearchName"].str(),
            searchAuthorRule = obj["rule_search_author"].str() ?: obj["ruleSearchAuthor"].str(),
            searchDetailUrlRule = obj["rule_search_detail_url"].str() ?: obj["ruleSearchDetailUrl"].str(),
            bookNameRule = obj["rule_book_name"].str() ?: obj["ruleBookName"].str(),
            bookAuthorRule = obj["rule_book_author"].str() ?: obj["ruleBookAuthor"].str(),
            coverUrlRule = obj["rule_cover_url"].str() ?: obj["ruleCoverUrl"].str(),
            introRule = obj["rule_book_intro"].str() ?: obj["ruleBookIntro"].str(),
            chapterUrlRule = obj["rule_chapter_url"].str() ?: obj["ruleChapterUrl"].str(),
            chapterListRule = obj["rule_chapter_list"].str() ?: obj["ruleChapterList"].str(),
            chapterNameRule = obj["rule_chapter_name"].str() ?: obj["ruleChapterName"].str(),
            contentRule = obj["rule_content"].str() ?: obj["ruleContent"].str(),
            enabled = obj["enabled"]?.asBoolean ?: true
        )
    }
}

/** 书源列表的反序列化器 */
class BookSourceListDeserializer : JsonDeserializer<List<BookSource>> {
    private val itemDeserializer = BookSourceDeserializer()

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): List<BookSource>? {
        if (json == null) return null

        return try {
            when {
                json.isJsonArray -> json.asJsonArray.mapNotNull { itemDeserializer.deserialize(it, null, null) }
                json.isJsonObject -> listOfNotNull(itemDeserializer.deserialize(json, null, null))
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
