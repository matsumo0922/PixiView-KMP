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
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.fade
import com.eygraber.compose.placeholder.material3.shimmer
import com.eygraber.compose.placeholder.placeholder
import io.github.aakira.napier.Napier
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class FanboxSessionId(
    val value: String = "",
)

val LocalFanboxSessionId = staticCompositionLocalOf { FanboxSessionId() }

val LocalFanboxMetadata = staticCompositionLocalOf { getFanboxMetadataDummy() }

@Composable
expect fun DrawableResource.asCoilImage(): Image

@Composable
fun ImageRequest.Builder.fanboxHeader(): ImageRequest.Builder {
    val sessionId = LocalFanboxSessionId.current.value

    httpHeaders(
        NetworkHeaders.Builder()
            .apply {
                set("origin", "https://www.fanbox.cc")
                set("referer", "https://www.fanbox.cc")
                set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")

                if (sessionId.isNotBlank()) {
                    set("Cookie", "FANBOXSESSID=$sessionId")
                }
            }
            .build(),
    )

    listener(
        onError = { _, e -> Napier.e(e.throwable) { "ImageRequest error: $e" } },
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

fun getFanboxMetadataDummy() = FanboxMetaData(
    apiUrl = "https://duckduckgo.com/?q=sodales",
    context = FanboxMetaData.Context(
        privacyPolicy = FanboxMetaData.Context.PrivacyPolicy(
            policyUrl = "https://search.yahoo.com/search?p=commodo",
            revisionHistoryUrl = "https://search.yahoo.com/search?p=lorem",
            shouldShowNotice = false,
            updateDate = "vix",
        ),
        user = FanboxMetaData.Context.User(
            creatorId = null,
            fanboxUserStatus = 8439,
            hasAdultContent = false,
            hasUnpaidPayments = false,
            iconUrl = null,
            isCreator = false,
            isMailAddressOutdated = false,
            isSupporter = false,
            lang = "quem",
            name = "Eliseo Gilliam",
            planCount = 3061,
            showAdultContent = false,
            userId = null,
        ),
    ),
    csrfToken = "doctus",
)
