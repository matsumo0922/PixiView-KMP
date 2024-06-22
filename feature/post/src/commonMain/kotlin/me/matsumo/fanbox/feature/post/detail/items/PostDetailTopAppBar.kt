package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailTopAppBar(
    state: LazyListState,
    postDetail: FanboxPostDetail,
    isShowHeader: Boolean,
    onClickNavigateUp: () -> Unit,
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
            IconButton(onClickNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(onClickMenu) {
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
        )
    )
}