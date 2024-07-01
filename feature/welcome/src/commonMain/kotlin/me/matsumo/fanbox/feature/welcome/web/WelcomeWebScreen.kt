package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.core.ui.welcome_login_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WelcomeWebScreen(
    navigateToLoginAlert: suspend (SimpleAlertContents) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeWebViewModel = koinViewModel(),
) {
    val fanboxUrl = "https://www.fanbox.cc/login"
    val fanboxRedirectUrl = "https://www.fanbox.cc/creators/find"

    val webViewState = rememberWebViewState("$fanboxUrl?return_to=$fanboxRedirectUrl")

    webViewState.webSettings.apply {
        isJavaScriptEnabled = true
        customUserAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        androidWebSettings.apply {
            domStorageEnabled = true
            isJavaScriptEnabled = true
        }
    }

    LaunchedEffect(true) {
        navigateToLoginAlert.invoke(SimpleAlertContents.Login)
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        Napier.d("lastLoadedUrl: ${webViewState.lastLoadedUrl}")

        if (webViewState.lastLoadedUrl == fanboxRedirectUrl) {
            val cookies = webViewState.cookieManager.getCookies("https://www.fanbox.cc")
            val cookieString = cookies.joinToString(";") { "${it.name}=${it.value}" }

            viewModel.saveCookie(cookieString)
            terminate.invoke()
        }
    }

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.welcome_login_title),
                onClickNavigation = { terminate.invoke() },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
            )

            (webViewState.loadingState as? LoadingState.Loading)?.also {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    progress = it.progress,
                )
            }
        }
    }
}
