package com.novelreader.app.domain.model

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 书源模型 - 兼容源阅读/阅读3.0 书源规范
 * 支持字段映射：bookSourceName → name 等
 */
data class BookSource(
    // 基础信息
    val name: String = "",
    val baseUrl: String = "",
    val group: String = "",
    val comment: String = "",
    val type: Int = 0, // 0=文字，1=音频，2=图片，3=TXT下载
    val enabled: Boolean = true,
    val customOrder: Int = 0,

    // URL相关
    val searchUrl: String = "",
    val exploreUrl: String = "",
    val bookUrlPattern: String = "",

    // 请求头
    val header: String = "",

    // 规则对象
    val ruleSearch: RuleSearch? = null,
    val ruleBookInfo: RuleBookInfo? = null,
    val ruleToc: RuleToc? = null,
    val ruleContent: RuleContent? = null,
    val ruleExplore: RuleExplore? = null,

    // 本地状态（不参与 JSON 反序列化）
    val id: Long = 0,
    val weight: Int = 0,
    val respondTime: Long = 0,
    val lastUpdateTime: Long = 0
) {
    companion object {
        /** 通用的 Gson 实例 */
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

    // 便捷方法：获取搜索书名规则
    fun getSearchNameRule(): String = ruleSearch?.name ?: ""
    // 便捷方法：获取搜索作者规则
    fun getSearchAuthorRule(): String = ruleSearch?.author ?: ""
    // 便捷方法：获取详情书名规则
    fun getBookNameRule(): String = ruleBookInfo?.name ?: ""
    // 便捷方法：获取详情作者规则
    fun getBookAuthorRule(): String = ruleBookInfo?.author ?: ""
    // 便捷方法：获取章节列表规则
    fun getChapterListRule(): String = ruleToc?.chapterList ?: ""
    // 便捷方法：获取正文规则
    fun getContentRule(): String = ruleContent?.content ?: ""
}

// ═══════════════════════════════════════════════════════════════════════════
// 规则数据类
// ═══════════════════════════════════════════════════════════════════════════

data class RuleSearch(
    val bookList: String = "",
    val name: String = "",
    val author: String = "",
    val kind: String = "",
    val intro: String = "",
    val wordCount: String = "",
    val bookUrl: String = "",
    val checkKeyWord: String = ""
)

data class RuleBookInfo(
    val name: String = "",
    val author: String = "",
    val kind: String = "",
    val wordCount: String = "",
    val intro: String = "",
    val coverUrl: String = "",
    val tocUrl: String = "",
    val downloadUrls: String = ""
)

data class RuleToc(
    val chapterList: String = "",
    val chapterName: String = "",
    val chapterUrl: String = "",
    val nextTocUrl: String = ""
)

data class RuleContent(
    val content: String = "",
    val nextContentUrl: String = ""
)

data class RuleExplore(
    val bookList: String = "",
    val name: String = "",
    val author: String = "",
    val kind: String = "",
    val intro: String = "",
    val bookUrl: String = ""
)

// ═══════════════════════════════════════════════════════════════════════════
// 反序列化器
// ═══════════════════════════════════════════════════════════════════════════

/** 单个书源的反序列化器：同时支持 snake_case 和 camelCase */
class BookSourceDeserializer : JsonDeserializer<BookSource> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): BookSource? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject

        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        fun JsonElement?.int(): Int = if (this != null && !isJsonNull) asInt else 0
        fun JsonElement?.bool(): Boolean = if (this != null && !isJsonNull) asBoolean else true
        fun JsonElement?.long(): Long = if (this != null && !isJsonNull) asLong else 0

        // 解析规则对象
        val ruleSearch = parseRuleSearch(obj["ruleSearch"] ?: obj["rule_search"])
        val ruleBookInfo = parseRuleBookInfo(obj["ruleBookInfo"] ?: obj["rule_book_info"])
        val ruleToc = parseRuleToc(obj["ruleToc"] ?: obj["rule_toc"])
        val ruleContent = parseRuleContent(obj["ruleContent"] ?: obj["rule_content"])
        val ruleExplore = parseRuleExplore(obj["ruleExplore"] ?: obj["rule_explore"])

        return BookSource(
            name = obj["bookSourceName"].str().ifBlank { obj["book_source_name"].str() },
            baseUrl = obj["bookSourceUrl"].str().ifBlank { obj["book_source_url"].str() },
            group = obj["bookSourceGroup"].str().ifBlank { obj["book_source_group"].str() },
            comment = obj["bookSourceComment"].str().ifBlank { obj["book_source_comment"].str() },
            type = (obj["bookSourceType"] ?: obj["book_source_type"]).int(),
            enabled = (obj["enabled"]).bool(),
            customOrder = (obj["customOrder"] ?: obj["custom_order"]).int(),
            searchUrl = obj["searchUrl"].str().ifBlank { obj["search_url"].str() },
            exploreUrl = obj["exploreUrl"].str().ifBlank { obj["explore_url"].str() },
            bookUrlPattern = obj["bookUrlPattern"].str().ifBlank { obj["book_url_pattern"].str() },
            header = obj["header"].str(),
            ruleSearch = ruleSearch,
            ruleBookInfo = ruleBookInfo,
            ruleToc = ruleToc,
            ruleContent = ruleContent,
            ruleExplore = ruleExplore,
            weight = (obj["weight"]).int(),
            respondTime = (obj["respondTime"] ?: obj["respond_time"]).long(),
            lastUpdateTime = (obj["lastUpdateTime"] ?: obj["last_update_time"]).long()
        )
    }

    private fun parseRuleSearch(json: JsonElement?): RuleSearch? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        return RuleSearch(
            bookList = obj["bookList"].str().ifBlank { obj["book_list"].str() },
            name = obj["name"].str(),
            author = obj["author"].str(),
            kind = obj["kind"].str(),
            intro = obj["intro"].str(),
            wordCount = obj["wordCount"].str().ifBlank { obj["word_count"].str() },
            bookUrl = obj["bookUrl"].str().ifBlank { obj["book_url"].str() },
            checkKeyWord = obj["checkKeyWord"].str().ifBlank { obj["check_key_word"].str() }
        )
    }

    private fun parseRuleBookInfo(json: JsonElement?): RuleBookInfo? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        return RuleBookInfo(
            name = obj["name"].str(),
            author = obj["author"].str(),
            kind = obj["kind"].str(),
            wordCount = obj["wordCount"].str().ifBlank { obj["word_count"].str() },
            intro = obj["intro"].str(),
            coverUrl = obj["coverUrl"].str().ifBlank { obj["cover_url"].str() },
            tocUrl = obj["tocUrl"].str().ifBlank { obj["toc_url"].str() },
            downloadUrls = obj["downloadUrls"].str().ifBlank { obj["download_urls"].str() }
        )
    }

    private fun parseRuleToc(json: JsonElement?): RuleToc? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        return RuleToc(
            chapterList = obj["chapterList"].str().ifBlank { obj["chapter_list"].str() },
            chapterName = obj["chapterName"].str().ifBlank { obj["chapter_name"].str() },
            chapterUrl = obj["chapterUrl"].str().ifBlank { obj["chapter_url"].str() },
            nextTocUrl = obj["nextTocUrl"].str().ifBlank { obj["next_toc_url"].str() }
        )
    }

    private fun parseRuleContent(json: JsonElement?): RuleContent? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        return RuleContent(
            content = obj["content"].str(),
            nextContentUrl = obj["nextContentUrl"].str().ifBlank { obj["next_content_url"].str() }
        )
    }

    private fun parseRuleExplore(json: JsonElement?): RuleExplore? {
        if (json == null || !json.isJsonObject) return null
        val obj = json.asJsonObject
        fun JsonElement?.str(): String = this?.asString?.takeIf { it.isNotBlank() } ?: ""
        return RuleExplore(
            bookList = obj["bookList"].str().ifBlank { obj["book_list"].str() },
            name = obj["name"].str(),
            author = obj["author"].str(),
            kind = obj["kind"].str(),
            intro = obj["intro"].str(),
            bookUrl = obj["bookUrl"].str().ifBlank { obj["book_url"].str() }
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
