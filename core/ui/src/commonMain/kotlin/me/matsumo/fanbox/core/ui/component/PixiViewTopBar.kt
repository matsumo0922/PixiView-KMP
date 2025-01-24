package me.matsumo.fanbox.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixiViewTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: ImageVector? = Icons.AutoMirrored.Filled.ArrowBack,
    actionsIcon: ImageVector? = Icons.Default.MoreVert,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    highlightColor: Color = MaterialTheme.colorScheme.surface,
    isTransparent: Boolean = false,
    isShowTitle: Boolean = true,
    onClickNavigation: (() -> Unit)? = null,
    onClickActions: (() -> Unit)? = null,
) {
    val colors = if (isTransparent) {
        TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    } else {
        TopAppBarDefaults.topAppBarColors()
    }

    val iconColors = if (isTransparent) {
        IconButtonDefaults.iconButtonColors(highlightColor.copy(alpha = 0.4f))
    } else {
        IconButtonDefaults.iconButtonColors()
    }

    TopAppBar(
        modifier = modifier,
        colors = colors,
        windowInsets = windowInsets,
        title = {
            if (title != null) {
                AnimatedVisibility(
                    visible = isShowTitle,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        navigationIcon = {
            if (onClickNavigation != null) {
                IconButton(
                    colors = iconColors,
                    onClick = onClickNavigation,
                ) {
                    navigationIcon?.let {
                        Icon(
                            imageVector = it,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        actions = {
            if (onClickActions != null) {
                IconButton(
                    colors = iconColors,
                    onClick = onClickActions,
                ) {
                    actionsIcon?.let {
                        Icon(
                            imageVector = it,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixiViewCenterAlignedTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    actionsIcon: ImageVector = Icons.Default.MoreVert,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isTransparent: Boolean = false,
    onClickNavigation: (() -> Unit)? = null,
    onClickActions: (() -> Unit)? = null,
) {
    val colors = if (isTransparent) {
        TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    } else {
        TopAppBarDefaults.topAppBarColors()
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = colors,
        windowInsets = windowInsets,
        title = {
            if (title != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                )
            }
        },
        navigationIcon = {
            if (onClickNavigation != null) {
                IconButton(onClick = onClickNavigation) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = null,
                    )
                }
            }
        },
        actions = {
            if (onClickActions != null) {
                IconButton(onClick = onClickActions) {
                    Icon(
                        imageVector = actionsIcon,
                        contentDescription = null,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
