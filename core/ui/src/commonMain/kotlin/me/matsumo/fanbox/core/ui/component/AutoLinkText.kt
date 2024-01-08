package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalTextApi::class)
@Composable
fun AutoLinkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val linkSpanStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
    )

    val linkString by remember(text) { mutableStateOf(makeLinkString(text, linkSpanStyle)) }

    ClickableText(
        text = linkString,
        onClick = {
            linkString.getUrlAnnotations(start = it, end = it).firstOrNull()?.let { range ->
                // context.startActivity(Intent(Intent.ACTION_VIEW, range.item.url.toUri()))
            }
        },
        modifier = modifier,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        style = if (color == Color.Unspecified) style else style.copy(color = color),
    )
}

@OptIn(ExperimentalTextApi::class)
private fun makeLinkString(text: String, linkStyle: SpanStyle): AnnotatedString = buildAnnotatedString {
    append(text)

    for (url in """http(s)?://([\w-]+.)+[\w-]+(/[\w-./?%&=]*)?""".toRegex().findAll(text)) {
        addUrlAnnotation(
            urlAnnotation = UrlAnnotation(url.value),
            start = url.range.first,
            end = url.range.last + 1,
        )

        addStyle(
            style = linkStyle,
            start = url.range.first,
            end = url.range.last + 1,
        )
    }
}
