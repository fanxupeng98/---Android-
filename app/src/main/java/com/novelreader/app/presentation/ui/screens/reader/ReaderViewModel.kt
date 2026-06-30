package com.novelreader.app.presentation.ui.screens.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.data.remote.BookParser
import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.model.Chapter
import com.novelreader.app.domain.repository.BookRepository
import com.novelreader.app.domain.repository.BookSourceRepository
import com.novelreader.app.domain.repository.ChapterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════
// 阅读器状态
// ═══════════════════════════════════════════════════════════════════════════

data class ReaderUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val currentChapterIndex: Int = 0,
    val currentChapter: Chapter? = null,
    val content: String = "",
    val isLoading: Boolean = false,
    val showChapterList: Boolean = false,
    val showSettings: Boolean = false,
    val showThemeEditor: Boolean = false,

    // 字体设置
    val fontSize: Int = 18,
    val lineHeight: Float = 1.8f,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.Default,

    // 翻页模式
    val pageMode: ReaderPageMode = ReaderPageMode.HorizontalSwipe,

    // 背景设置
    val backgroundColor: ReaderBackground = ReaderBackground.White,

    // 自定义主题
    val customThemes: List<ReaderTheme> = defaultCustomThemes,
    val activeTheme: ReaderTheme? = null
)

// ═══════════════════════════════════════════════════════════════════════════
// 翻页模式
// ═══════════════════════════════════════════════════════════════════════════

enum class ReaderPageMode(
    val displayName: String,
    val description: String
) {
    Simulation("仿真翻书", "模拟真实书籍翻页效果"),
    VerticalScroll("上下翻页", "点击屏幕中央区域滚动阅读"),
    HorizontalSwipe("左右滑动", "左右滑动翻页，点击左右区域快速翻页")
}

// ═══════════════════════════════════════════════════════════════════════════
// 背景颜色预设
// ═══════════════════════════════════════════════════════════════════════════

enum class ReaderBackground(
    val colorName: String,
    val backgroundColor: Long,
    val textColor: Long
) {
    White("白色",   0xFFFFFFFF, 0xFF000000),
    Sepia("护眼黄", 0xFFF5ECD7, 0xFF5C4B32),
    Night("夜间黑", 0xFF121212, 0xFFCCCCCC),
    DarkBlue("深蓝", 0xFF1A2332, 0xFFB0C4DE),
    DarkGreen("墨绿", 0xFF1A2F1A, 0xFF90EE90)
}

// ═══════════════════════════════════════════════════════════════════════════
// 字体选择
// ═══════════════════════════════════════════════════════════════════════════

enum class ReaderFontFamily(val displayName: String) {
    Default("默认"),
    Serif("宋体/衬线"),
    SansSerif("黑体/无衬线"),
    Monospace("等宽")
}

// ═══════════════════════════════════════════════════════════════════════════
// 自定义阅读主题
// ═══════════════════════════════════════════════════════════════════════════

data class ReaderTheme(
    val id: String,
    val name: String,
    val backgroundColor: Long,
    val textColor: Long,
    val accentColor: Long,
    val isBuiltIn: Boolean = false
)

val defaultCustomThemes = listOf(
    // 预设主题
    ReaderTheme(
        id = "default_white",
        name = "默认白",
        backgroundColor = 0xFFFFFFFF,
        textColor = 0xFF333333,
        accentColor = 0xFF6D5E0F,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "sepia",
        name = "羊皮纸",
        backgroundColor = 0xFFF5ECD7,
        textColor = 0xFF5C4B32,
        accentColor = 0xFF8B7355,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "night",
        name = "夜间模式",
        backgroundColor = 0xFF121212,
        textColor = 0xFFCCCCCC,
        accentColor = 0xFFDEC74C,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "ocean",
        name = "海洋蓝",
        backgroundColor = 0xFF1A2332,
        textColor = 0xFFB0C4DE,
        accentColor = 0xFF4A90D9,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "forest",
        name = "森林绿",
        backgroundColor = 0xFF1A2F1A,
        textColor = 0xFF90EE90,
        accentColor = 0xFF228B22,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "pink",
        name = "少女粉",
        backgroundColor = 0xFFFFF0F5,
        textColor = 0xFF6B4E5C,
        accentColor = 0xFFFF69B4,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "purple",
        name = "星空紫",
        backgroundColor = 0xFF2D1B3D,
        textColor = 0xFFDDA0DD,
        accentColor = 0xFF9370DB,
        isBuiltIn = true
    ),
    ReaderTheme(
        id = "gold",
        name = "奢华金",
        backgroundColor = 0xFF2C2416,
        textColor = 0xFFD4AF37,
        accentColor = 0xFFFFD700,
        isBuiltIn = true
    )
)

// ═══════════════════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val bookSourceRepository: BookSourceRepository,
    private val chapterRepository: ChapterRepository,
    private val bookParser: BookParser
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L
    private val initialChapterIndex: Int = savedStateHandle.get<Int>("chapterIndex") ?: 0

    private val _uiState = MutableStateFlow(
        ReaderUiState(currentChapterIndex = initialChapterIndex)
    )
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val book = bookRepository.getBookById(bookId)
            if (book != null) {
                _uiState.update { it.copy(book = book) }

                val chapters = chapterRepository.getChaptersByBookId(bookId)
                if (chapters.isEmpty()) {
                    loadChaptersFromSource(book)
                } else {
                    _uiState.update { it.copy(chapters = chapters) }
                    loadChapterContent(initialChapterIndex)
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadChaptersFromSource(book: Book) {
        val source = bookSourceRepository.getSourceById(book.sourceId)
        if (source != null && book.detailUrl != null) {
            val chapters = bookParser.getChapterList(book, source)
            _uiState.update { it.copy(chapters = chapters) }
            chapters.forEach { chapterRepository.saveChapter(it) }
            loadChapterContent(initialChapterIndex)
        }
    }

    fun loadChapterContent(chapterIndex: Int) {
        viewModelScope.launch {
            val chapters = _uiState.value.chapters
            if (chapterIndex < 0 || chapterIndex >= chapters.size) return@launch

            val chapter = chapters[chapterIndex]
            _uiState.update {
                it.copy(
                    currentChapterIndex = chapterIndex,
                    currentChapter = chapter,
                    content = "",
                    isLoading = true
                )
            }

            // 尝试加载缓存
            val cachedChapter = chapterRepository.getChapterContent(chapter.id)
            if (cachedChapter?.content != null && cachedChapter.isCached) {
                _uiState.update {
                    it.copy(content = cachedChapter.content!!, isLoading = false)
                }
                updateReadProgress()
                return@launch
            }

            // 从网络获取
            val book = _uiState.value.book ?: return@launch
            val source = bookSourceRepository.getSourceById(book.sourceId) ?: return@launch

            val content = bookParser.getChapterContent(chapter, source)
            if (content != null) {
                chapterRepository.updateChapterContent(chapter.id, content)
                _uiState.update { it.copy(content = content) }
            } else {
                _uiState.update { it.copy(content = "加载失败，请重试") }
            }

            _uiState.update { it.copy(isLoading = false) }
            updateReadProgress()
        }
    }

    private fun updateReadProgress() {
        viewModelScope.launch {
            val book = _uiState.value.book ?: return@launch
            val index = _uiState.value.currentChapterIndex
            val chapters = _uiState.value.chapters

            val progress = if (chapters.isNotEmpty()) {
                index.toFloat() / chapters.size
            } else 0f

            bookRepository.updateBook(
                book.copy(
                    lastReadChapter = index,
                    lastReadChapterTitle = _uiState.value.currentChapter?.title,
                    lastReadTime = System.currentTimeMillis(),
                    readProgress = progress
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 翻页控制
    // ─────────────────────────────────────────────────────────────────

    fun nextChapter() {
        val nextIndex = _uiState.value.currentChapterIndex + 1
        if (nextIndex < _uiState.value.chapters.size) {
            loadChapterContent(nextIndex)
        }
    }

    fun prevChapter() {
        val prevIndex = _uiState.value.currentChapterIndex - 1
        if (prevIndex >= 0) {
            loadChapterContent(prevIndex)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 面板控制
    // ─────────────────────────────────────────────────────────────────

    fun toggleChapterList() {
        _uiState.update {
            it.copy(
                showChapterList = !it.showChapterList,
                showSettings = false,
                showThemeEditor = false
            )
        }
    }

    fun toggleSettings() {
        _uiState.update {
            it.copy(
                showSettings = !it.showSettings,
                showChapterList = false,
                showThemeEditor = false
            )
        }
    }

    fun toggleThemeEditor() {
        _uiState.update {
            it.copy(
                showThemeEditor = !it.showThemeEditor,
                showSettings = false,
                showChapterList = false
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 字体设置
    // ─────────────────────────────────────────────────────────────────

    fun setFontSize(size: Int) {
        _uiState.update { it.copy(fontSize = size.coerceIn(12, 32)) }
    }

    fun setLineHeight(height: Float) {
        _uiState.update { it.copy(lineHeight = height.coerceIn(1.2f, 2.5f)) }
    }

    fun setFontFamily(family: ReaderFontFamily) {
        _uiState.update { it.copy(fontFamily = family) }
    }

    // ─────────────────────────────────────────────────────────────────
    // 翻页模式
    // ─────────────────────────────────────────────────────────────────

    fun setPageMode(mode: ReaderPageMode) {
        _uiState.update { it.copy(pageMode = mode) }
    }

    // ─────────────────────────────────────────────────────────────────
    // 背景设置
    // ─────────────────────────────────────────────────────────────────

    fun setBackground(color: ReaderBackground) {
        _uiState.update {
            it.copy(
                backgroundColor = color,
                activeTheme = null  // 取消自定义主题选中
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 自定义主题
    // ─────────────────────────────────────────────────────────────────

    fun applyTheme(theme: ReaderTheme) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun addCustomTheme(theme: ReaderTheme) {
        _uiState.update {
            it.copy(customThemes = it.customThemes + theme)
        }
    }

    fun removeCustomTheme(themeId: String) {
        _uiState.update {
            it.copy(
                customThemes = it.customThemes.filter { t -> t.id != themeId },
                activeTheme = if (it.activeTheme?.id == themeId) null else it.activeTheme
            )
        }
    }
}
