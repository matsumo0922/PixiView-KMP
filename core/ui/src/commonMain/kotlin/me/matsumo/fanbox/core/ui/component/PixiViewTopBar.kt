package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    navigationIcon: ImageVector? = Icons.Filled.ArrowBack,
    actionsIcon: ImageVector? = Icons.Default.MoreVert,
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

    TopAppBar(
        modifier = modifier,
        colors = colors,
        windowInsets = windowInsets,
        title = {
            if (title != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        navigationIcon = {
            if (onClickNavigation != null) {
                IconButton(onClick = onClickNavigation) {
                    navigationIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        actions = {
            if (onClickActions != null) {
                IconButton(onClick = onClickActions) {
                    actionsIcon?.let {
                        Icon(
                            imageVector = it,
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
    navigationIcon: ImageVector = Icons.Filled.ArrowBack,
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
