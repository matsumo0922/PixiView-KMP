package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.AnnotatedString
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun String.toRichHtmlString(): AnnotatedString {
    val state = rememberRichTextState()

    LaunchedEffect(this) {
        state.setHtml(this@toRichHtmlString)
    }

    return state.annotatedString
}