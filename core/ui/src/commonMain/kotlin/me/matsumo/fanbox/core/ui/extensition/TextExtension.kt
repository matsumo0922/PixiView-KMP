package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.text.AnnotatedString
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun String.toRichHtmlString(): AnnotatedString {
    val state = rememberRichTextState()

    SideEffect(this) {
        state.setHtml(this@toRichHtmlString)
    }

    return state.annotatedString
}
