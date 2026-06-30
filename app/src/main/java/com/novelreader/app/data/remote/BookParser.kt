package com.novelreader.app.data.remote

import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.model.Chapter
import com.novelreader.app.domain.model.SearchResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookParser @Inject constructor(
    private val htmlFetcher: HtmlFetcher
) {
    
    /**
     * 搜索书籍
     */
    suspend fun search(keyword: String, source: BookSource): List<SearchResult> {
        if (source.searchUrl.isBlank()) return emptyList()
        
        return try {
            val url = buildSearchUrl(source.searchUrl, keyword)
            val doc = htmlFetcher.fetchDocument(url).getOrNull() ?: return emptyList()
            parseSearchResults(doc, source)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 构建搜索 URL
     */
    private fun buildSearchUrl(searchUrlTemplate: String, keyword: String): String {
        return searchUrlTemplate
            .replace("{keyword}", java.net.URLEncoder.encode(keyword, "UTF-8"))
            .replace("{keywordEncoded}", java.net.URLEncoder.encode(keyword, "UTF-8"))
    }
    
    /**
     * 解析搜索结果
     */
    private fun parseSearchResults(doc: Document, source: BookSource): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            // 通用选择器尝试
            val selectors = listOf(
                source.searchNameRule,
                ".book-name", ".bookname", "h3 a", ".title a",
                ".book-list li", ".bookitem", ".search-list li",
                ".result-item", ".novel-item"
            )
            
            val elements = findElements(doc, selectors)
            
            for ((index, element) in elements.withIndex()) {
                val name = extractText(element, source.searchNameRule)
                    ?: element.selectFirst("a")?.text()
                    ?: element.selectFirst("h3, .name, .title")?.text()
                    ?: continue
                
                val author = extractText(element, source.searchAuthorRule)
                    ?: element.selectFirst(".author, .writer")?.text()
                    ?: ""
                
                val detailUrl = extractAttr(element, source.searchDetailUrlRule, "href")
                    ?: element.selectFirst("a")?.attr("href")
                    ?: ""
                
                if (name.isNotBlank() && detailUrl.isNotBlank()) {
                    val fullUrl = if (detailUrl.startsWith("http")) detailUrl 
                        else "${source.baseUrl.removeSuffix("/")}/$detailUrl"
                    
                    results.add(SearchResult(
                        name = name.trim(),
                        author = author.trim(),
                        detailUrl = fullUrl,
                        sourceName = source.name,
                        sourceId = source.id
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return results
    }
    
    /**
     * 获取书籍详情
     */
    suspend fun getBookDetail(url: String, source: BookSource): Book? {
        return try {
            val doc = htmlFetcher.fetchDocument(url).getOrNull() ?: return null
            parseBookDetail(doc, url, source)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseBookDetail(doc: Document, url: String, source: BookSource): Book {
        val name = extractText(doc, source.bookNameRule)
            ?: doc.selectFirst("h1, .book-title, .title")?.text()
            ?: "未知书名"
        
        val author = extractText(doc, source.bookAuthorRule)
            ?: doc.selectFirst(".author, .book-author")?.text()
            ?: "未知作者"
        
        val coverUrl = extractAttr(doc, source.coverUrlRule, "src")
            ?: extractAttr(doc, source.coverUrlRule, "data-src")
            ?: doc.selectFirst("img.cover, .book-cover img")?.attr("src")
        
        val intro = extractText(doc, source.introRule)
            ?: doc.selectFirst(".intro, .description, .book-desc")?.text()
        
        return Book(
            name = name.trim(),
            author = author.trim().removePrefix("作者：").removePrefix("作者:"),
            coverUrl = coverUrl?.let { if (it.startsWith("//")) "https:$it" else it },
            intro = intro?.trim(),
            detailUrl = url,
            sourceId = source.id,
            sourceName = source.name
        )
    }
    
    /**
     * 获取章节列表
     */
    suspend fun getChapterList(book: Book, source: BookSource): List<Chapter> {
        if (source.chapterListRule.isBlank() && source.chapterUrlRule.isBlank()) return emptyList()
        
        return try {
            // 先尝试直接获取章节列表
            val listUrl = if (source.chapterUrlRule.isNotBlank()) {
                "${source.baseUrl.removeSuffix("/")}/${source.chapterUrlRule}"
            } else {
                book.detailUrl
            }
            
            val listUrlNonNull: String = listUrl ?: return emptyList()
            val doc = htmlFetcher.fetchDocument(listUrlNonNull).getOrNull() ?: return emptyList()
            parseChapterList(doc, book.id, source)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseChapterList(doc: Document, bookId: Long, source: BookSource): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        
        try {
            // 尝试多种选择器
            val listSelector = source.chapterListRule.ifBlank { 
                ".chapter-list, .chapter-list a, .catalog a, #chapter-list a" 
            }
            
            val elements = doc.select(listSelector)
            
            for ((index, element) in elements.withIndex()) {
                val title = extractText(element, source.chapterNameRule)
                    ?: element.text()
                    ?: continue
                
                var url = extractAttr(element, null, "href")
                    ?: continue
                
                // 补全 URL
                if (!url.startsWith("http")) {
                    url = "${source.baseUrl.removeSuffix("/")}/$url"
                }
                
                chapters.add(Chapter(
                    bookId = bookId,
                    index = index,
                    title = title.trim(),
                    url = url
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return chapters
    }
    
    /**
     * 获取章节正文
     */
    suspend fun getChapterContent(chapter: Chapter, source: BookSource): String? {
        return try {
            val doc = htmlFetcher.fetchDocument(chapter.url).getOrNull() ?: return null
            extractContent(doc, source.contentRule)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractContent(doc: Document, contentRule: String): String? {
        try {
            // 清理无关内容
            doc.select("script, style, .ad, .advertisement, .comment, nav, .nav, .toolbar").remove()
            
            val content = if (contentRule.isNotBlank()) {
                doc.selectFirst(contentRule)?.text()
                    ?: doc.selectFirst("#content, .content, .chapter-content")?.text()
            } else {
                doc.selectFirst("#content, .content, .chapter-content")?.text()
                    ?: doc.body()?.text()
            }
            
            // 清理格式
            return content
                ?.replace("\\s+".toRegex(), "\n")
                ?.lines()
                ?.filter { it.length > 10 }
                ?.joinToString("\n\n")
        } catch (e: Exception) {
            return null
        }
    }
    
    // ========== 工具方法 ==========
    
    private fun findElements(doc: Document, selectors: List<String>): Elements {
        for (selector in selectors) {
            if (selector.isBlank()) continue
            try {
                val elements = doc.select(selector)
                if (elements.isNotEmpty()) return elements
            } catch (e: Exception) { /* ignore */ }
        }
        return Elements()
    }
    
    private fun extractText(element: Element, rule: String?): String? {
        if (rule.isNullOrBlank()) return null
        return try {
            element.selectFirst(rule)?.text()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }
    
    private fun extractAttr(element: Element, rule: String?, attr: String): String? {
        if (rule.isNullOrBlank()) {
            return element.attr(attr).takeIf { it.isNotBlank() }
        }
        return try {
            element.selectFirst(rule)?.attr(attr)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }
}
