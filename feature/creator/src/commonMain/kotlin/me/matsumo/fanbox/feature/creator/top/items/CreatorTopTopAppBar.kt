package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.revealable
import me.matsumo.fanbox.feature.creator.top.CreatorTopRevealKeys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorTopTopAppBar(
    title: String,
    isShowTitle: Boolean,
    windowInsets: WindowInsets,
    revealState: RevealState,
    onClickNavigation: () -> Unit,
    onClickSearch: () -> Unit,
    onClickActions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = {
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
        },
        navigationIcon = {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                onClick = onClickNavigation,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                modifier = Modifier.revealable(
                    key = CreatorTopRevealKeys.Search,
                    state = revealState,
                    shape = RevealShape.Circle,
                ),
                colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                onClick = onClickSearch,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                )
            }

            IconButton(
                colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                onClick = onClickActions,
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                )
            }
        },
        windowInsets = windowInsets,
    )
}
