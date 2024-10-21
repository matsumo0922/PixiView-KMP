package me.matsumo.fanbox.feature.post.search.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.post_search_placeholder
import me.matsumo.fanbox.feature.post.search.PostSearchMode
import me.matsumo.fanbox.feature.post.search.PostSearchQuery
import me.matsumo.fanbox.feature.post.search.parseQuery
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostSearchTopBar(
    query: String,
    initialQuery: String,
    scrollBehavior: TopAppBarScrollBehavior?,
    onClickTerminate: () -> Unit,
    onClickSearch: (PostSearchQuery) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var queryText by rememberSaveable(query) { mutableStateOf(query) }
    val focusRequester = remember { FocusRequester() }

    val colorTransitionFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            MaterialTheme.colorScheme.secondaryContainer,
            FastOutLinearInEasing.transform(fraction),
        ),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "appBarContainerColor",
    )

    LaunchedEffect(true) {
        if (initialQuery.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(
        modifier = modifier,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(color = appBarContainerColor)
                    .padding(16.dp, 10.dp),
            ) {
                BasicTextField(
                    modifier = Modifier
                        .clickable { keyboardController?.show() }
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    value = queryText,
                    onValueChange = { queryText = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            parseQuery(queryText).also {
                                if (it.mode != PostSearchMode.Unknown) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    onClickSearch.invoke(it)
                                }
                            }
                        },
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(Modifier.weight(1f)) {
                                if (queryText.isEmpty()) {
                                    Text(
                                        text = stringResource(Res.string.post_search_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                innerTextField()
                            }

                            Icon(
                                modifier = Modifier
                                    .alpha(if (queryText.isNotEmpty()) 1f else 0f)
                                    .clip(CircleShape)
                                    .clickable(queryText.isNotEmpty()) { queryText = "" },
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                            )
                        }
                    },
                    visualTransformation = QueryTransformation(
                        tagStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    ),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickTerminate) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

private class QueryTransformation(val tagStyle: SpanStyle) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedQuery(text.text, tagStyle),
            offsetMapping = OffsetMapping.Identity,
        )
    }

    private fun buildAnnotatedQuery(query: String, tagStyle: SpanStyle): AnnotatedString = buildAnnotatedString {
        append(query)

        for (url in Regex("#\\S+").findAll(query)) {
            addStyle(
                style = tagStyle,
                start = url.range.first,
                end = url.range.last + 1,
            )
        }
    }
}
