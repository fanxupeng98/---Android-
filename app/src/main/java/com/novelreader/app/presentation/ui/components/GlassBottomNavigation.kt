package com.novelreader.app.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novelreader.app.presentation.ui.theme.GlassBorderDark
import com.novelreader.app.presentation.ui.theme.GlassBorderLight
import com.novelreader.app.presentation.ui.theme.GlassHighlightDark
import com.novelreader.app.presentation.ui.theme.GlassHighlightLight
import com.novelreader.app.presentation.ui.theme.GlassSurfaceDark
import com.novelreader.app.presentation.ui.theme.GlassSurfaceLight

// ═══════════════════════════════════════════════════════════════════════════
// 液态玻璃效果包装器
// ═══════════════════════════════════════════════════════════════════════════

/**
 * 液态玻璃（iOS/miui 风格磨砂玻璃）
 * @param modifier 修饰符
 * @param cornerRadius 圆角，默认 20dp（iOS 风格圆润边角）
 * @param blurRadius 模糊半径，默认 30dp
 * @param overlayOpacity 叠加层透明度
 * @param content 内容
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    blurRadius: Dp = 30.dp,
    overlayOpacity: Float = 0.6f,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val glassColor = if (isDark) GlassSurfaceDark else GlassSurfaceLight
    val highlightColor = if (isDark) GlassHighlightDark else GlassHighlightLight
    val borderColor = if (isDark) GlassBorderDark else GlassBorderLight

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        highlightColor.copy(alpha = overlayOpacity * 0.5f),
                        glassColor.copy(alpha = overlayOpacity),
                        glassColor.copy(alpha = overlayOpacity * 0.8f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// iOS 风格底部导航栏（液态玻璃）
// ═══════════════════════════════════════════════════════════════════════════

data class GlassNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val selectedIcon: ImageVector? = null
)

@Composable
fun GlassBottomNavigationBar(
    items: List<GlassNavItem>,
    currentRoute: String?,
    onItemClick: (GlassNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val navHeight = 80.dp
    val bottomPadding = with(density) { 28.dp }

    // 整体导航容器
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navHeight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .offset(y = -bottomPadding),
            cornerRadius = 24.dp,
            blurRadius = 32.dp,
            overlayOpacity = 0.72f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    GlassNavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassNavBarItem(
    item: GlassNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    // 选中态动画
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val contentColor = if (isSelected) primaryColor else onSurfaceColor
    val icon = if (isSelected && item.selectedIcon != null) item.selectedIcon else item.icon

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 选中态背景光晕
            if (animatedScale > 0f) {
                Box(
                    modifier = Modifier
                        .size((28 + animatedScale * 8).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.25f * animatedScale),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = item.label,
                modifier = Modifier.size(26.dp),
                tint = contentColor
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 辅助函数
// ═══════════════════════════════════════════════════════════════════════════

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
