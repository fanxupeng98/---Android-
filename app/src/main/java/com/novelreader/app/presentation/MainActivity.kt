package com.novelreader.app.presentation

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novelreader.app.presentation.navigation.NavGraph
import com.novelreader.app.presentation.navigation.Screen
import com.novelreader.app.presentation.ui.components.GlassBottomNavigationBar
import com.novelreader.app.presentation.ui.components.GlassNavItem
import com.novelreader.app.presentation.ui.theme.NovelReaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ═══════════════════════════════════════════════════════════
        // Android 16 边缘到边缘配置
        // ═══════════════════════════════════════════════════════════
        enableEdgeToEdge()

        // 确保内容绘制到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Android 15+ 小白条防窥处理
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        setContent {
            NovelReaderTheme {
                NovelReaderApp()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 主应用入口
// ═══════════════════════════════════════════════════════════════════════════

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    data object Bookshelf : BottomNavItem(
        route = Screen.Bookshelf.route,
        icon = Icons.Default.LibraryBooks,
        selectedIcon = Icons.Filled.LibraryBooks,
        label = "书架"
    )
    data object Search : BottomNavItem(
        route = Screen.Search.route,
        icon = Icons.Default.Search,
        selectedIcon = Icons.Filled.Search,
        label = "搜索"
    )
    data object Source : BottomNavItem(
        route = Screen.BookSource.route,
        icon = Icons.Default.Source,
        selectedIcon = Icons.Filled.Source,
        label = "书源"
    )
}

@Composable
fun NovelReaderApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val view = LocalView.current
    val imeInsets = WindowInsets.ime
    val systemBarsInsets = WindowInsets.systemBars

    val bottomNavItems = listOf(
        BottomNavItem.Bookshelf,
        BottomNavItem.Search,
        BottomNavItem.Source
    )

    val glassNavItems = bottomNavItems.map {
        GlassNavItem(
            route = it.route,
            icon = it.icon,
            selectedIcon = it.selectedIcon,
            label = it.label
        )
    }

    // ═══════════════════════════════════════════════════════════
    // 判断当前是否为主页面（需要底部导航）
    // ═══════════════════════════════════════════════════════════
    val showBottomBar = currentRoute in listOf(
        Screen.Bookshelf.route,
        Screen.Search.route,
        Screen.BookSource.route
    )

    // ═══════════════════════════════════════════════════════════
    // 判断当前是否为阅读页面（全屏沉浸）
    // ═══════════════════════════════════════════════════════════
    val isReaderScreen = currentRoute?.startsWith(Screen.Reader.route.substringBefore("/")) == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // ═══════════════════════════════════════════════════════════
        // Android 16: 主页面底部导航（iOS 液态玻璃风格）
        // ═══════════════════════════════════════════════════════════
        bottomBar = {
            if (showBottomBar) {
                GlassBottomNavigationBar(
                    items = glassNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        // ═══════════════════════════════════════════════════════════
        // 阅读页面：完全不显示底部栏（真正的沉浸）
        // ═══════════════════════════════════════════════════════════
        contentWindowInsets = if (isReaderScreen) {
            // 阅读页面：内边距设为0，全屏沉浸
            WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        } else if (showBottomBar) {
            // 主页面：顶部系统栏沉浸，底部留导航栏高度
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        } else {
            // 其他页面（如详情页）：顶部沉浸，底部系统栏
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        }
    ) { innerPadding ->
        // 阅读页面传0 padding，其他页面正常传
        val effectivePadding = if (isReaderScreen) {
            PaddingValues(0.dp)
        } else if (showBottomBar) {
            // 主页面底部栏不需要padding（玻璃导航悬浮在内容上）
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                end = innerPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                bottom = 0.dp  // 底部由液态玻璃导航覆盖
            )
        } else {
            innerPadding
        }

        NavGraph(
            navController = navController,
            startDestination = Screen.Bookshelf.route
        )
    }
}
