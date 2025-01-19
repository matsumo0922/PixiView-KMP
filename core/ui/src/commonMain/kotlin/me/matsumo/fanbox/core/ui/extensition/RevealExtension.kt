package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.RevealCanvasState
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import me.matsumo.fanbox.core.common.util.waitFor

val LocalRevealCanvasState = staticCompositionLocalOf { RevealCanvasState() }

suspend fun RevealState.revealByStep(
    keys: List<Key>,
    onCompleted: () -> Unit = {},
) {
    runCatching {
        for (key in keys) {
            reveal(key)
            waitFor { !isVisible }
        }

        onCompleted()
    }.onFailure {
        for (key in keys) {
            removeRevealable(key)
        }
    }
}

@Composable
fun OverlayText(
    text: String,
    arrow: Arrow,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Balloon(
        modifier = modifier.padding(8.dp),
        arrow = arrow,
        backgroundColor = containerColor,
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}
