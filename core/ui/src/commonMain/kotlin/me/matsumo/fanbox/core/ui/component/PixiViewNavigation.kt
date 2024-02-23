@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBar
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBarItem
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun PixiViewNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    if (currentPlatform == Platform.Android) {
        NavigationBar(
            modifier = modifier,
            contentColor = PixiViewNavigationDefaults.navigationContentColor(),
            tonalElevation = 0.dp,
            content = content,
        )
    } else {
        AdaptiveNavigationBar(
            modifier = modifier,
            content = content,
        )
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun RowScope.PixiViewNavigationBarItem(
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    if (currentPlatform == Platform.Android) {
        NavigationBarItem(
            modifier = modifier,
            selected = isSelected,
            onClick = onClick,
            icon = icon,
            label = label,
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PixiViewNavigationDefaults.navigationSelectedItemColor(),
                unselectedIconColor = PixiViewNavigationDefaults.navigationContentColor(),
                selectedTextColor = PixiViewNavigationDefaults.navigationSelectedItemColor(),
                unselectedTextColor = PixiViewNavigationDefaults.navigationContentColor(),
                indicatorColor = PixiViewNavigationDefaults.navigationIndicatorColor(),
            ),
        )
    } else {
        AdaptiveNavigationBarItem(
            selected = isSelected,
            icon = icon,
            label = label,
            onClick = onClick,
        )
    }
}

@Composable
fun PixiViewNavigationRail(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        contentColor = PixiViewNavigationDefaults.navigationContentColor(),
        content = content,
    )
}

@Composable
fun ColumnScope.PixiViewNavigationRailItem(
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    NavigationRailItem(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        icon = icon,
        label = label,
        alwaysShowLabel = false,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = PixiViewNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = PixiViewNavigationDefaults.navigationContentColor(),
            selectedTextColor = PixiViewNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = PixiViewNavigationDefaults.navigationContentColor(),
            indicatorColor = PixiViewNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

object PixiViewNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}
