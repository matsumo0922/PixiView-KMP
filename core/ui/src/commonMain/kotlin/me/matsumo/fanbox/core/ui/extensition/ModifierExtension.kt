package me.matsumo.fanbox.core.ui.extensition

/*
 * MIT License
 *
 * Copyright (c) 2022 Albert Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

fun Modifier.drawHorizontalScrollbar(
    state: ScrollState,
    reverseScrolling: Boolean = false,
): Modifier = drawScrollbar(state, Orientation.Horizontal, reverseScrolling)

fun Modifier.drawVerticalScrollbar(
    state: ScrollState,
    reverseScrolling: Boolean = false,
): Modifier = drawScrollbar(state, Orientation.Vertical, reverseScrolling)

private fun Modifier.drawScrollbar(
    state: ScrollState,
    orientation: Orientation,
    reverseScrolling: Boolean,
): Modifier = drawScrollbar(
    orientation,
    reverseScrolling,
) { reverseDirection, atEnd, color, alpha ->
    if (state.maxValue > 0) {
        val canvasSize = if (orientation == Orientation.Horizontal) size.width else size.height
        val totalSize = canvasSize + state.maxValue
        val thumbSize = canvasSize / totalSize * canvasSize
        val startOffset = state.value / totalSize * canvasSize
        drawScrollbar(
            orientation,
            reverseDirection,
            atEnd,
            color,
            alpha,
            thumbSize,
            startOffset,
        )
    }
}

fun Modifier.drawHorizontalScrollbar(
    state: LazyListState,
    reverseScrolling: Boolean = false,
): Modifier = drawScrollbar(state, Orientation.Horizontal, reverseScrolling)

fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    reverseScrolling: Boolean = false,
): Modifier = drawScrollbar(state, Orientation.Vertical, reverseScrolling)

private fun Modifier.drawScrollbar(
    state: LazyListState,
    orientation: Orientation,
    reverseScrolling: Boolean,
): Modifier = drawScrollbar(
    orientation,
    reverseScrolling,
) { reverseDirection, atEnd, color, alpha ->
    val layoutInfo = state.layoutInfo
    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val items = layoutInfo.visibleItemsInfo
    val itemsSize = items.fastSumBy { it.size }
    if (items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize) {
        val estimatedItemSize = if (items.isEmpty()) 0f else itemsSize.toFloat() / items.size
        val totalSize = estimatedItemSize * layoutInfo.totalItemsCount
        val canvasSize = if (orientation == Orientation.Horizontal) size.width else size.height
        val thumbSize = viewportSize / totalSize * canvasSize
        val startOffset = if (items.isEmpty()) {
            0f
        } else {
            items.first().run {
                (estimatedItemSize * index - offset) / totalSize * canvasSize
            }
        }
        drawScrollbar(
            orientation,
            reverseDirection,
            atEnd,
            color,
            alpha,
            thumbSize,
            startOffset,
        )
    }
}

fun Modifier.drawVerticalScrollbar(
    state: LazyGridState,
    spanCount: Int,
    reverseScrolling: Boolean = false,
): Modifier = drawScrollbar(
    Orientation.Vertical,
    reverseScrolling,
) { reverseDirection, atEnd, color, alpha ->
    val layoutInfo = state.layoutInfo
    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val items = layoutInfo.visibleItemsInfo
    val rowCount = (items.size + spanCount - 1) / spanCount
    var itemsSize = 0
    for (i in 0 until rowCount) {
        itemsSize += items[i * spanCount].size.height
    }
    if (items.size < layoutInfo.totalItemsCount || itemsSize > viewportSize) {
        val estimatedItemSize = if (rowCount == 0) 0f else itemsSize.toFloat() / rowCount
        val totalRow = (layoutInfo.totalItemsCount + spanCount - 1) / spanCount
        val totalSize = estimatedItemSize * totalRow
        val canvasSize = size.height
        val thumbSize = viewportSize / totalSize * canvasSize
        val startOffset = if (rowCount == 0) {
            0f
        } else {
            items.first().run {
                val rowIndex = index / spanCount
                (estimatedItemSize * rowIndex - offset.y) / totalSize * canvasSize
            }
        }
        drawScrollbar(
            Orientation.Vertical,
            reverseDirection,
            atEnd,
            color,
            alpha,
            thumbSize,
            startOffset,
        )
    }
}

private fun DrawScope.drawScrollbar(
    orientation: Orientation,
    reverseDirection: Boolean,
    atEnd: Boolean,
    color: Color,
    alpha: () -> Float,
    thumbSize: Float,
    startOffset: Float,
) {
    val thicknessPx = Thickness.toPx()
    val topLeft = if (orientation == Orientation.Horizontal) {
        Offset(
            if (reverseDirection) size.width - startOffset - thumbSize else startOffset,
            if (atEnd) size.height - thicknessPx else 0f,
        )
    } else {
        Offset(
            if (atEnd) size.width - thicknessPx else 0f,
            if (reverseDirection) size.height - startOffset - thumbSize else startOffset,
        )
    }
    val size = if (orientation == Orientation.Horizontal) {
        Size(thumbSize, thicknessPx)
    } else {
        Size(thicknessPx, thumbSize)
    }

    drawRect(
        color = color,
        topLeft = topLeft,
        size = size,
        alpha = alpha(),
    )
}

private fun Modifier.drawScrollbar(
    orientation: Orientation,
    reverseScrolling: Boolean,
    onDraw: DrawScope.(
        reverseDirection: Boolean,
        atEnd: Boolean,
        color: Color,
        alpha: () -> Float,
    ) -> Unit,
): Modifier = composed {
    val scrolled = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }
    val nestedScrollConnection = remember(orientation, scrolled) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = if (orientation == Orientation.Horizontal) consumed.x else consumed.y
                if (delta != 0f) scrolled.tryEmit(Unit)
                return Offset.Zero
            }
        }
    }

    val alpha = remember { Animatable(0f) }
    LaunchedEffect(scrolled, alpha) {
        scrolled.collectLatest {
            alpha.snapTo(1f)
            delay(300)
            alpha.animateTo(0f, animationSpec = FadeOutAnimationSpec)
        }
    }

    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val reverseDirection = if (orientation == Orientation.Horizontal) {
        if (isLtr) reverseScrolling else !reverseScrolling
    } else {
        reverseScrolling
    }
    val atEnd = if (orientation == Orientation.Vertical) isLtr else true

    val color = BarColor

    Modifier
        .nestedScroll(nestedScrollConnection)
        .drawWithContent {
            drawContent()
            onDraw(reverseDirection, atEnd, color, alpha::value)
        }
}

private val BarColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

private val Thickness = 4.dp
private val FadeOutAnimationSpec = tween<Float>(durationMillis = 250)

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.marquee(): Modifier {
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawFadedEdge(leftEdge = true)
            drawFadedEdge(leftEdge = false)
        }
        .basicMarquee(Int.MAX_VALUE)
        .padding(horizontal = 8.dp)
}

fun ContentDrawScope.drawFadedEdge(leftEdge: Boolean) {
    val edgeWidthPx = 8.dp.toPx()
    drawRect(
        topLeft = Offset(if (leftEdge) 0f else size.width - edgeWidthPx, 0f),
        size = Size(edgeWidthPx, size.height),
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = if (leftEdge) 0f else size.width,
            endX = if (leftEdge) edgeWidthPx else size.width - edgeWidthPx,
        ),
        blendMode = BlendMode.DstIn,
    )
}
