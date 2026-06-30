package com.novelreader.app.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.novelreader.app.domain.model.RuleBookInfo
import com.novelreader.app.domain.model.RuleContent
import com.novelreader.app.domain.model.RuleExplore
import com.novelreader.app.domain.model.RuleSearch
import com.novelreader.app.domain.model.RuleToc

class BookSourceConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromRuleSearch(rule: RuleSearch?): String {
        return gson.toJson(rule)
    }

    @TypeConverter
    fun toRuleSearch(json: String): RuleSearch? {
        return if (json.isBlank()) null else gson.fromJson(json, RuleSearch::class.java)
    }

    @TypeConverter
    fun fromRuleBookInfo(rule: RuleBookInfo?): String {
        return gson.toJson(rule)
    }

    @TypeConverter
    fun toRuleBookInfo(json: String): RuleBookInfo? {
        return if (json.isBlank()) null else gson.fromJson(json, RuleBookInfo::class.java)
    }

    @TypeConverter
    fun fromRuleToc(rule: RuleToc?): String {
        return gson.toJson(rule)
    }

    @TypeConverter
    fun toRuleToc(json: String): RuleToc? {
        return if (json.isBlank()) null else gson.fromJson(json, RuleToc::class.java)
    }

    @TypeConverter
    fun fromRuleContent(rule: RuleContent?): String {
        return gson.toJson(rule)
    }

    @TypeConverter
    fun toRuleContent(json: String): RuleContent? {
        return if (json.isBlank()) null else gson.fromJson(json, RuleContent::class.java)
    }

    @TypeConverter
    fun fromRuleExplore(rule: RuleExplore?): String {
        return gson.toJson(rule)
    }

    @TypeConverter
    fun toRuleExplore(json: String): RuleExplore? {
        return if (json.isBlank()) null else gson.fromJson(json, RuleExplore::class.java)
    }
}
