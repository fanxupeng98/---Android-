package com.novelreader.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.model.RuleBookInfo
import com.novelreader.app.domain.model.RuleContent
import com.novelreader.app.domain.model.RuleExplore
import com.novelreader.app.domain.model.RuleSearch
import com.novelreader.app.domain.model.RuleToc

@Entity(tableName = "book_sources")
data class BookSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val groupName: String = "",
    val comment: String = "",
    val type: Int = 0,
    val enabled: Boolean = true,
    val customOrder: Int = 0,
    
    // URL相关
    val searchUrl: String = "",
    val exploreUrl: String = "",
    val bookUrlPattern: String = "",
    val header: String = "",
    
    // 规则对象（JSON字符串存储）
    val ruleSearchJson: String = "",
    val ruleBookInfoJson: String = "",
    val ruleTocJson: String = "",
    val ruleContentJson: String = "",
    val ruleExploreJson: String = "",
    
    val weight: Int = 0,
    val respondTime: Long = 0,
    val lastUpdateTime: Long = 0
) {
    fun toDomainModel(): BookSource {
        val gson = Gson()
        return BookSource(
            id = id,
            name = name,
            baseUrl = baseUrl,
            group = groupName,
            comment = comment,
            type = type,
            enabled = enabled,
            customOrder = customOrder,
            searchUrl = searchUrl,
            exploreUrl = exploreUrl,
            bookUrlPattern = bookUrlPattern,
            header = header,
            ruleSearch = if (ruleSearchJson.isNotBlank()) {
                gson.fromJson(ruleSearchJson, RuleSearch::class.java)
            } else null,
            ruleBookInfo = if (ruleBookInfoJson.isNotBlank()) {
                gson.fromJson(ruleBookInfoJson, RuleBookInfo::class.java)
            } else null,
            ruleToc = if (ruleTocJson.isNotBlank()) {
                gson.fromJson(ruleTocJson, RuleToc::class.java)
            } else null,
            ruleContent = if (ruleContentJson.isNotBlank()) {
                gson.fromJson(ruleContentJson, RuleContent::class.java)
            } else null,
            ruleExplore = if (ruleExploreJson.isNotBlank()) {
                gson.fromJson(ruleExploreJson, RuleExplore::class.java)
            } else null,
            weight = weight,
            respondTime = respondTime,
            lastUpdateTime = lastUpdateTime
        )
    }

    companion object {
        fun fromDomainModel(source: BookSource): BookSourceEntity {
            val gson = Gson()
            return BookSourceEntity(
                id = source.id,
                name = source.name,
                baseUrl = source.baseUrl,
                groupName = source.group,
                comment = source.comment,
                type = source.type,
                enabled = source.enabled,
                customOrder = source.customOrder,
                searchUrl = source.searchUrl,
                exploreUrl = source.exploreUrl,
                bookUrlPattern = source.bookUrlPattern,
                header = source.header,
                ruleSearchJson = source.ruleSearch?.let { gson.toJson(it) } ?: "",
                ruleBookInfoJson = source.ruleBookInfo?.let { gson.toJson(it) } ?: "",
                ruleTocJson = source.ruleToc?.let { gson.toJson(it) } ?: "",
                ruleContentJson = source.ruleContent?.let { gson.toJson(it) } ?: "",
                ruleExploreJson = source.ruleExplore?.let { gson.toJson(it) } ?: "",
                weight = source.weight,
                respondTime = source.respondTime,
                lastUpdateTime = source.lastUpdateTime
            )
        }
    }
}
