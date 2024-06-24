@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.fade
import com.eygraber.compose.placeholder.material3.shimmer
import com.eygraber.compose.placeholder.placeholder
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class FanboxCookie(
    val cookie: String = "",
)

val LocalFanboxCookie = staticCompositionLocalOf { FanboxCookie() }

val LocalFanboxMetadata = staticCompositionLocalOf { FanboxMetaData.dummy() }

@Composable
expect fun DrawableResource.asCoilImage(): Image

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageRequest.Builder.fanboxHeader(): ImageRequest.Builder {
    val cookie = LocalFanboxCookie.current.cookie

    httpHeaders(
        NetworkHeaders.Builder()
            .apply {
                set("origin", "https://www.fanbox.cc")
                set("referer", "https://www.fanbox.cc")
                set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")

                if (cookie.isNotBlank()) {
                    set("Cookie", cookie)
                }
            }
            .build()
    )

    return this
}

@Composable
fun SimmerPlaceHolder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                highlight = PlaceholderHighlight.shimmer(),
                shape = RectangleShape,
            ),
    )
}

@Composable
fun FadePlaceHolder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                highlight = PlaceholderHighlight.fade(),
                shape = RectangleShape,
            ),
    )
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun IndicatorPlaceHolder(
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        AdaptiveCircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
