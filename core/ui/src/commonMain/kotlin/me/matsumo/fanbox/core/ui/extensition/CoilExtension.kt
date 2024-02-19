@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.Image
import coil3.request.ImageRequest
import coil3.request.httpHeaders
import dev.icerock.moko.resources.ImageResource
import io.ktor.http.headers
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail

@Immutable
data class FanboxCookie(
    val cookie: String = "",
)

val LocalFanboxCookie = staticCompositionLocalOf { FanboxCookie() }

val LocalFanboxMetadata = staticCompositionLocalOf { FanboxMetaData.dummy() }

interface ImageDownloader {
    suspend fun downloadImage(item: FanboxPostDetail.ImageItem, updateCallback: (Float) -> Unit = {}): Boolean
    suspend fun downloadFile(item: FanboxPostDetail.FileItem, updateCallback: (Float) -> Unit = {}): Boolean
}

@Composable
expect fun ImageResource.asCoilImage(): Image

@Composable
fun ImageRequest.Builder.fanboxHeader(): ImageRequest.Builder {
    val cookie = LocalFanboxCookie.current.cookie

    httpHeaders(
        headers {
            append("origin", "https://www.fanbox.cc")
            append("referer", "https://www.fanbox.cc")
            append("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")

            if (cookie.isNotBlank()) {
                append("Cookie", cookie)
            }
        }
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
            /*.placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                highlight = PlaceholderHighlight.shimmer(),
                shape = RectangleShape,
            ),*/
    )
}

@Composable
fun FadePlaceHolder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            /*.placeholder(
                visible = true,
                color = MaterialTheme.colorScheme.surfaceVariant,
                highlight = PlaceholderHighlight.fade(),
                shape = RectangleShape,
            ),*/
    )
}

@Composable
fun IndicatorPlaceHolder(
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
