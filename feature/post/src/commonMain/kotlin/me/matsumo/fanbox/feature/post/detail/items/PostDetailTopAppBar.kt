package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailTopAppBar(
    state: LazyListState,
    postDetail: FanboxPostDetail,
    bodyTransState: TranslationState<FanboxPostDetail>,
    isShowHeader: Boolean,
    onClickNavigateUp: () -> Unit,
    onClickTranslate: (FanboxPostDetail) -> Unit,
    onClickMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTransparent by remember { mutableStateOf(true) }

    val containerColor by animateColorAsState(
        targetValue = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "containerColor",
    )

    val scrolledContainerColor by animateColorAsState(
        targetValue = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "scrolledContainerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isTransparent) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "contentColor",
    )

    val threshold = remember(state) {
        val headerSize = if (isShowHeader) 1 else 0
        val itemSize = when (val content = postDetail.body) {
            is FanboxPostDetail.Body.Article -> content.blocks.size
            is FanboxPostDetail.Body.File -> content.files.size
            is FanboxPostDetail.Body.Image -> content.images.size
            else -> 0
        }

        headerSize + itemSize
    }

    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo }.collect {
            isTransparent = (state.firstVisibleItemIndex < threshold)
        }
    }

    TopAppBar(
        modifier = modifier,
        title = { /* no title */ },
        navigationIcon = {
            IconButton(
                onClick = onClickNavigateUp,
                colors = IconButtonDefaults.iconButtonColors(scrolledContainerColor.copy(alpha = 0.3f)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onClickTranslate.invoke(postDetail) },
                colors = IconButtonDefaults.iconButtonColors(scrolledContainerColor.copy(alpha = 0.3f)),
            ) {
                if (bodyTransState is TranslationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(6.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                    )
                }
            }

            IconButton(
                onClick = onClickMenu,
                colors = IconButtonDefaults.iconButtonColors(scrolledContainerColor.copy(alpha = 0.3f)),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
            navigationIconContentColor = contentColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
    )
}
