package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.theme.applyTonalElevation

@Composable
fun CoordinatorScaffold(
    header: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    color: Color = MaterialTheme.colorScheme.surface,
    onClickNavigateUp: (() -> Unit)? = null,
    onClickMenu: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.() -> Unit,
) {
    var appBarAlpha by remember { mutableFloatStateOf(0f) }
    var topSectionHeight by remember { mutableIntStateOf(100) }
    var toolBarHeight by remember { mutableIntStateOf(64) }

    Box(modifier.background(color)) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                state = listState,
            ) {
                item {
                    header.invoke(
                        Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { topSectionHeight = it.size.height },
                    )
                }

                content(this)
            }

            bottomBar.invoke()
        }

        CoordinatorToolBar(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { toolBarHeight = it.size.height },
            color = MaterialTheme.colorScheme.applyTonalElevation(
                backgroundColor = MaterialTheme.colorScheme.surface,
                elevation = 3.dp,
            ),
            backgroundAlpha = if (appBarAlpha.isNaN()) 0f else appBarAlpha,
            onClickNavigateUp = onClickNavigateUp,
            onClickMenu = onClickMenu,
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect {
            val index = listState.firstVisibleItemIndex
            val disableArea = topSectionHeight * 0.7f
            val alpha = if (index == 0) (listState.firstVisibleItemScrollOffset.toDouble() - disableArea) / (topSectionHeight - disableArea) else 1

            appBarAlpha = (alpha.toFloat() * 2).coerceIn(0f..1f)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordinatorToolBar(
    color: Color,
    backgroundAlpha: Float,
    onClickNavigateUp: (() -> Unit)?,
    onClickMenu: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = color.copy(backgroundAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (backgroundAlpha > 0.9f) 4.dp else 0.dp,
    ) {
        PixiViewTopBar(
            modifier = Modifier.statusBarsPadding(),
            isTransparent = true,
            windowInsets = WindowInsets(0, 0, 0, 0),
            onClickNavigation = onClickNavigateUp,
            onClickActions = onClickMenu,
        )
    }
}
