package com.novelreader.app.presentation.ui.screens.reader

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs
import kotlin.math.min

// ═══════════════════════════════════════════════════════════════════════════
// 阅读器页面 - Android 16 + 三种翻页模式 + 自定义主题
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    initialChapterIndex: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (Int) -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    // ═══════════════════════════════════════════════════════════════════
    // Android 16 全屏沉浸
    // ═══════════════════════════════════════════════════════════════════
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            try {
                @Suppress("UNCHECKED_CAST")
                (window.decorView.findViewById<View>(android.R.id.navigationBarBackground))
                    ?.visibility = View.GONE
            } catch (_: Exception) { }
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 颜色计算（支持自定义主题）
    // ═══════════════════════════════════════════════════════════════════
    // 用 remember 计算 Long 颜色值（remember 块中不能调用 @Composable 的 Color()）
    val rawBgColor = remember(uiState.activeTheme, uiState.backgroundColor) {
        uiState.activeTheme?.backgroundColor ?: uiState.backgroundColor.backgroundColor
    }
    val rawTxtColor = remember(uiState.activeTheme, uiState.backgroundColor) {
        uiState.activeTheme?.textColor ?: uiState.backgroundColor.textColor
    }
    val rawAccentColor = remember(uiState.activeTheme) {
        uiState.activeTheme?.accentColor ?: 0xFF6200EEL
    }
    // 在 Composable 作用域内构造 Color 对象
    val backgroundColor = Color(rawBgColor)
    val textColor = Color(rawTxtColor)
    val accentColor = Color(rawAccentColor)

    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // ═══════════════════════════════════════════════════════════
        // 翻页模式内容区
        // ═══════════════════════════════════════════════════════════
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.pageMode) {
                ReaderPageMode.Simulation -> SimulationPageTurnReader(
                    content = uiState.content,
                    fontSize = uiState.fontSize,
                    lineHeight = uiState.lineHeight,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    accentColor = accentColor,
                    fontFamily = uiState.fontFamily,
                    isLoading = uiState.isLoading,
                    chapters = uiState.chapters,
                    currentChapterIndex = uiState.currentChapterIndex,
                    currentChapter = uiState.currentChapter,
                    navBarHeight = getNavBarHeight(view),
                    onPrev = { viewModel.prevChapter() },
                    onNext = { viewModel.nextChapter() }
                )

                ReaderPageMode.VerticalScroll -> VerticalScrollReader(
                    content = uiState.content,
                    fontSize = uiState.fontSize,
                    lineHeight = uiState.lineHeight,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    accentColor = accentColor,
                    fontFamily = uiState.fontFamily,
                    isLoading = uiState.isLoading,
                    chapters = uiState.chapters,
                    currentChapterIndex = uiState.currentChapterIndex,
                    currentChapter = uiState.currentChapter,
                    navBarHeight = getNavBarHeight(view),
                    onPrev = { viewModel.prevChapter() },
                    onNext = { viewModel.nextChapter() }
                )

                ReaderPageMode.HorizontalSwipe -> HorizontalSwipeReader(
                    content = uiState.content,
                    fontSize = uiState.fontSize,
                    lineHeight = uiState.lineHeight,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    accentColor = accentColor,
                    fontFamily = uiState.fontFamily,
                    isLoading = uiState.isLoading,
                    chapters = uiState.chapters,
                    currentChapterIndex = uiState.currentChapterIndex,
                    currentChapter = uiState.currentChapter,
                    navBarHeight = getNavBarHeight(view),
                    onChapterChange = { viewModel.loadChapterContent(it) }
                )
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 顶部控制栏
        // ═══════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(color = backgroundColor.copy(alpha = 0.96f)) {
                Column {
                    Spacer(modifier = Modifier.height(getStatusBarHeight(view)))
                    TopAppBar(
                        title = {
                            Text(
                                uiState.book?.name ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = textColor
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = textColor
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleChapterList() }) {
                                Icon(Icons.Default.List, contentDescription = "目录", tint = textColor)
                            }
                            IconButton(onClick = { viewModel.toggleSettings() }) {
                                Icon(Icons.Default.Settings, contentDescription = "设置", tint = textColor)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 设置面板
        // ═══════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = uiState.showSettings,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderSettings(
                fontSize = uiState.fontSize,
                onFontSizeChange = { viewModel.setFontSize(it) },
                lineHeight = uiState.lineHeight,
                onLineHeightChange = { viewModel.setLineHeight(it) },
                fontFamily = uiState.fontFamily,
                onFontFamilyChange = { viewModel.setFontFamily(it) },
                pageMode = uiState.pageMode,
                onPageModeChange = { viewModel.setPageMode(it) },
                background = uiState.backgroundColor,
                onBackgroundChange = { viewModel.setBackground(it) },
                activeTheme = uiState.activeTheme,
                onThemeEditorClick = { viewModel.toggleThemeEditor() },
                textColor = textColor,
                backgroundColor = backgroundColor,
                accentColor = accentColor,
                navBarHeight = getNavBarHeight(view)
            )
        }

        // ═══════════════════════════════════════════════════════════
        // 主题编辑器
        // ═══════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = uiState.showThemeEditor,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ThemeEditor(
                themes = uiState.customThemes,
                activeTheme = uiState.activeTheme,
                backgroundColor = backgroundColor,
                textColor = textColor,
                accentColor = accentColor,
                onThemeSelect = { viewModel.applyTheme(it) },
                onAddTheme = { viewModel.addCustomTheme(it) },
                onRemoveTheme = { viewModel.removeCustomTheme(it) },
                navBarHeight = getNavBarHeight(view)
            )
        }

        // ═══════════════════════════════════════════════════════════
        // 章节列表
        // ═══════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = uiState.showChapterList,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            ChapterListPanel(
                chapters = uiState.chapters,
                currentIndex = uiState.currentChapterIndex,
                onChapterSelect = { viewModel.loadChapterContent(it); viewModel.toggleChapterList() },
                onClose = { viewModel.toggleChapterList() },
                textColor = textColor,
                backgroundColor = backgroundColor,
                accentColor = accentColor,
                navBarHeight = getNavBarHeight(view)
            )
        }

        // ═══════════════════════════════════════════════════════════
        // 控制栏自动隐藏
        // ═══════════════════════════════════════════════════════════
        LaunchedEffect(showControls, uiState.showSettings, uiState.showChapterList, uiState.showThemeEditor) {
            val anyVisible = showControls || uiState.showSettings ||
                    uiState.showChapterList || uiState.showThemeEditor
            if (anyVisible) {
                kotlinx.coroutines.delay(4000)
                showControls = false
                viewModel.toggleSettings().also { viewModel.toggleSettings() }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 仿真翻书模式
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SimulationPageTurnReader(
    content: String,
    fontSize: Int,
    lineHeight: Float,
    textColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    fontFamily: ReaderFontFamily,
    isLoading: Boolean,
    chapters: List<com.novelreader.app.domain.model.Chapter>,
    currentChapterIndex: Int,
    currentChapter: com.novelreader.app.domain.model.Chapter?,
    navBarHeight: androidx.compose.ui.unit.Dp,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    // 翻页动画状态
    var isFlipping by remember { mutableStateOf(false) }
    var flipDirection by remember { mutableStateOf(FlipDirection.None) }
    var dragProgress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isFlipping) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        finishedListener = {
            if (isFlipping && flipDirection != FlipDirection.None) {
                if (flipDirection == FlipDirection.Forward) onNext() else onPrev()
                isFlipping = false
                flipDirection = FlipDirection.None
                dragProgress = 0f
            }
        },
        label = "flip"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenWidth = size.width
                        when {
                            offset.x < screenWidth / 3 -> {
                                if (!isFlipping) {
                                    flipDirection = FlipDirection.Back
                                    isFlipping = true
                                }
                            }
                            offset.x > screenWidth * 2 / 3 -> {
                                if (!isFlipping) {
                                    flipDirection = FlipDirection.Forward
                                    isFlipping = true
                                }
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragProgress) > 0.3f && !isFlipping) {
                            if (dragProgress > 0) {
                                flipDirection = FlipDirection.Forward
                                isFlipping = true
                            } else {
                                flipDirection = FlipDirection.Back
                                isFlipping = true
                            }
                        }
                        dragProgress = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragProgress = (dragAmount / size.width).coerceIn(-1f, 1f)
                    }
                )
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = accentColor
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(getStatusBarHeight(androidx.compose.ui.platform.LocalView.current) + 56.dp))

                currentChapter?.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = content,
                    color = textColor,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * lineHeight).sp,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            // 仿真翻书效果：缩放 + 阴影
                            val progress = animatedProgress
                            val direction = if (flipDirection == FlipDirection.Forward) 1f else -1f

                            // 轻微3D透视
                            cameraDistance = 12f * density
                            rotationY = direction * progress * 15f

                            // 翻页时轻微缩放
                            scaleX = 1f - (progress * 0.05f)
                            scaleY = 1f - (progress * 0.05f)

                            // 阴影效果
                            val shadowAlpha = progress * 0.3f
                            if (flipDirection == FlipDirection.Forward) {
                                alpha = 1f - progress * 0.2f
                            }
                        }
                        .verticalScroll(rememberScrollState())
                )

                // 页码
                PageIndicator(
                    current = currentChapterIndex + 1,
                    total = chapters.size,
                    textColor = textColor,
                    navBarHeight = navBarHeight
                )
            }
        }

        // 翻页提示文字
        if (isFlipping) {
            Box(
                modifier = Modifier
                    .align(if (flipDirection == FlipDirection.Forward) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        alpha = animatedProgress * 0.5f
                    }
            ) {
                Icon(
                    imageVector = if (flipDirection == FlipDirection.Forward)
                        Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

private enum class FlipDirection { None, Forward, Back }

// ═══════════════════════════════════════════════════════════════════════════
// 上下滚动模式
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun VerticalScrollReader(
    content: String,
    fontSize: Int,
    lineHeight: Float,
    textColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    fontFamily: ReaderFontFamily,
    isLoading: Boolean,
    chapters: List<com.novelreader.app.domain.model.Chapter>,
    currentChapterIndex: Int,
    currentChapter: com.novelreader.app.domain.model.Chapter?,
    navBarHeight: androidx.compose.ui.unit.Dp,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    val view = androidx.compose.ui.platform.LocalView.current

    LaunchedEffect(currentChapterIndex) {
        scrollState.scrollTo(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = accentColor
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(getStatusBarHeight(view) + 56.dp))

                currentChapter?.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = content,
                    color = textColor,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * lineHeight).sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(navBarHeight + 32.dp))

                // 章节导航按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onPrev,
                        enabled = currentChapterIndex > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null)
                        Text("上一章")
                    }

                    OutlinedButton(
                        onClick = onNext,
                        enabled = currentChapterIndex < chapters.size - 1,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                    ) {
                        Text("下一章")
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }

                PageIndicator(
                    current = currentChapterIndex + 1,
                    total = chapters.size,
                    textColor = textColor,
                    navBarHeight = navBarHeight
                )

                Spacer(modifier = Modifier.height(navBarHeight + 16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 左右滑动模式
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalSwipeReader(
    content: String,
    fontSize: Int,
    lineHeight: Float,
    textColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    fontFamily: ReaderFontFamily,
    isLoading: Boolean,
    chapters: List<com.novelreader.app.domain.model.Chapter>,
    currentChapterIndex: Int,
    currentChapter: com.novelreader.app.domain.model.Chapter?,
    navBarHeight: androidx.compose.ui.unit.Dp,
    onChapterChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentChapterIndex,
        pageCount = { chapters.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentChapterIndex) {
            onChapterChange(pagerState.currentPage)
        }
    }

    LaunchedEffect(currentChapterIndex) {
        if (pagerState.currentPage != currentChapterIndex) {
            pagerState.animateScrollToPage(currentChapterIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val chapter = chapters.getOrNull(page)
            val isCurrentPage = page == pagerState.currentPage

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(getStatusBarHeight(androidx.compose.ui.platform.LocalView.current) + 56.dp))

                    chapter?.let {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    val displayContent = if (isCurrentPage) content else chapter?.title ?: ""

                    Text(
                        text = displayContent,
                        color = textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * lineHeight).sp,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )

                    PageIndicator(
                        current = page + 1,
                        total = chapters.size,
                        textColor = textColor,
                        navBarHeight = navBarHeight
                    )
                }
            }
        }

        // 加载指示器
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = accentColor
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 页码指示器
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun PageIndicator(
    current: Int,
    total: Int,
    textColor: Color,
    navBarHeight: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "",
            color = textColor.copy(alpha = 0.3f),
            fontSize = 12.sp
        )
        Text(
            text = "$current / $total",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Text(
            text = "",
            color = textColor.copy(alpha = 0.3f),
            fontSize = 12.sp
        )
    }
    Spacer(modifier = Modifier.height(navBarHeight + 8.dp))
}

// ═══════════════════════════════════════════════════════════════════════════
// 阅读设置面板
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ReaderSettings(
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    lineHeight: Float,
    onLineHeightChange: (Float) -> Unit,
    fontFamily: ReaderFontFamily,
    onFontFamilyChange: (ReaderFontFamily) -> Unit,
    pageMode: ReaderPageMode,
    onPageModeChange: (ReaderPageMode) -> Unit,
    background: ReaderBackground,
    onBackgroundChange: (ReaderBackground) -> Unit,
    activeTheme: ReaderTheme?,
    onThemeEditorClick: () -> Unit,
    textColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    navBarHeight: androidx.compose.ui.unit.Dp
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.97f),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp, end = 24.dp,
                    top = 20.dp,
                    bottom = navBarHeight + 20.dp
                )
        ) {
            // ─────────────────────────────────────────────────────────
            // 翻页模式
            // ─────────────────────────────────────────────────────────
            Text("翻页模式", color = textColor, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderPageMode.entries.forEach { mode ->
                    FilterChip(
                        selected = pageMode == mode,
                        onClick = { onPageModeChange(mode) },
                        label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // ─────────────────────────────────────────────────────────
            // 字体大小
            // ─────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("字体大小", color = textColor, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onFontSizeChange(fontSize - 2) },
                    enabled = fontSize > 12
                ) {
                    Icon(Icons.Default.TextDecrease, "减小", tint = textColor)
                }
                Text(
                    text = "$fontSize",
                    color = textColor,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { onFontSizeChange(fontSize + 2) },
                    enabled = fontSize < 32
                ) {
                    Icon(Icons.Default.TextIncrease, "增大", tint = textColor)
                }
            }

            // 行高
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("行间距", color = textColor, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Slider(
                    value = lineHeight,
                    onValueChange = onLineHeightChange,
                    valueRange = 1.2f..2.5f,
                    steps = 5,
                    modifier = Modifier.width(160.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }

            // ─────────────────────────────────────────────────────────
            // 背景颜色
            // ─────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("背景色", color = textColor, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                ReaderBackground.entries.forEach { bg ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(bg.backgroundColor),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (background == bg && activeTheme == null) 2.dp else 1.dp,
                                color = if (background == bg && activeTheme == null)
                                    accentColor else textColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onBackgroundChange(bg) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            // ─────────────────────────────────────────────────────────
            // 主题编辑器入口
            // ─────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onThemeEditorClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
            ) {
                Icon(Icons.Default.Palette, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (activeTheme != null) "当前主题: ${activeTheme.name}" else "自定义主题"
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 主题编辑器
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ThemeEditor(
    themes: List<ReaderTheme>,
    activeTheme: ReaderTheme?,
    backgroundColor: Color,
    textColor: Color,
    accentColor: Color,
    onThemeSelect: (ReaderTheme) -> Unit,
    onAddTheme: (ReaderTheme) -> Unit,
    onRemoveTheme: (String) -> Unit,
    navBarHeight: androidx.compose.ui.unit.Dp
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.97f),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
                .padding(
                    start = 24.dp, end = 24.dp,
                    top = 20.dp,
                    bottom = navBarHeight + 20.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择主题", color = textColor, style = MaterialTheme.typography.titleMedium)
                Text(
                    "共 ${themes.size} 个主题",
                    color = textColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主题网格
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { theme ->
                            ThemeCard(
                                theme = theme,
                                isSelected = activeTheme?.id == theme.id,
                                accentColor = accentColor,
                                onSelect = { onThemeSelect(theme) },
                                onRemove = if (!theme.isBuiltIn) {
                                    { onRemoveTheme(theme.id) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 填充空白
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 快速创建自定义主题按钮
            TextButton(
                onClick = {
                    // 生成一个随机主题
                    val randomColors = listOf(
                        Triple(0xFF2C3E50, 0xFFECF0F1, 0xFFE74C3C), // 午夜巴黎
                        Triple(0xFF1B1464, 0xFFF7DC6F, 0xFFF39C12), // 金紫
                        Triple(0xFF1B1B2F, 0xFFEAEAEA, 0xFFEA5455), // 复古红
                        Triple(0xFF2C3333, 0xFFF8F9FA, 0xFF00D2FF)  // 冰蓝
                    )
                    val (bg, txt, acc) = randomColors.random()
                    onAddTheme(
                        ReaderTheme(
                            id = "custom_${System.currentTimeMillis()}",
                            name = "自定义 ${themes.size + 1}",
                            backgroundColor = bg,
                            textColor = txt,
                            accentColor = acc,
                            isBuiltIn = false
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("随机生成自定义主题")
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ReaderTheme,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val themeBg = Color(theme.backgroundColor)
    val themeTxt = Color(theme.textColor)
    val themeAcc = Color(theme.accentColor)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else themeBg.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(themeBg)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 颜色预览
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(themeBg, CircleShape)
                    .border(0.5.dp, themeTxt.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(themeTxt, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(themeAcc, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = theme.name,
            color = themeTxt,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!theme.isBuiltIn && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "删除",
                    tint = themeTxt.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 章节列表面板
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterListPanel(
    chapters: List<com.novelreader.app.domain.model.Chapter>,
    currentIndex: Int,
    onChapterSelect: (Int) -> Unit,
    onClose: () -> Unit,
    textColor: Color,
    backgroundColor: Color,
    accentColor: Color,
    navBarHeight: androidx.compose.ui.unit.Dp
) {
    Surface(
        color = backgroundColor,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("目录", style = MaterialTheme.typography.titleMedium, color = textColor)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "关闭", tint = textColor)
                }
            }

            HorizontalDivider(color = textColor.copy(alpha = 0.15f))

            val listState = rememberLazyListState()

            LaunchedEffect(currentIndex) {
                listState.animateScrollToItem(maxOf(0, currentIndex - 2))
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navBarHeight)
            ) {
                items(chapters) { chapter ->
                    Surface(
                        onClick = { onChapterSelect(chapter.index) },
                        color = if (chapter.index == currentIndex)
                            accentColor.copy(alpha = 0.12f)
                        else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chapter.title,
                                color = if (chapter.index == currentIndex) accentColor else textColor,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (chapter.isCached) {
                                Icon(
                                    Icons.Default.OfflinePin,
                                    contentDescription = "已缓存",
                                    modifier = Modifier.size(16.dp),
                                    tint = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 辅助函数
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun getStatusBarHeight(view: View): androidx.compose.ui.unit.Dp {
    return remember(view) {
        val resourceId = view.context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        if (resourceId > 0) {
            val heightPx = view.context.resources.getDimensionPixelSize(resourceId)
            androidx.compose.ui.unit.Dp(heightPx / view.context.resources.displayMetrics.density)
        } else 24.dp
    }
}

@Composable
private fun getNavBarHeight(view: View): androidx.compose.ui.unit.Dp {
    return remember(view) {
        val resourceId = view.context.resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        if (resourceId > 0) {
            val heightPx = view.context.resources.getDimensionPixelSize(resourceId)
            androidx.compose.ui.unit.Dp(heightPx / view.context.resources.displayMetrics.density)
        } else 48.dp
    }
}
