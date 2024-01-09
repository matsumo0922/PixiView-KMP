package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.view.LoadingView
import moe.tlaster.precompose.koin.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WelcomeWebScreen(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeWebViewModel = koinViewModel(WelcomeWebViewModel::class)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarSuccessMessage = stringResource(MR.strings.welcome_login_toast_success)

    val fanboxUrl = "https://www.fanbox.cc/login"
    val fanboxRedirectUrl = "https://www.fanbox.cc/creators/find"

    var isLoggedIn by remember { mutableStateOf(false) }
    val webViewState = rememberWebViewState("$fanboxUrl?return_to=$fanboxRedirectUrl")

    webViewState.webSettings.apply {
        isJavaScriptEnabled = true

        androidWebSettings.apply {
            domStorageEnabled = true
            isJavaScriptEnabled = true
        }
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        if (webViewState.lastLoadedUrl == fanboxRedirectUrl) {
            val cookies = webViewState.cookieManager.getCookies(fanboxRedirectUrl)
            val cookieString = cookies.joinToString(";") { "${it.name}=${it.value}" }

            viewModel.saveCookie(cookieString)

            isLoggedIn = true
            terminate.invoke()

            snackbarHostState.showSnackbar(snackbarSuccessMessage)
        }
    }

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(MR.strings.welcome_login_title),
                onClickNavigation = { terminate.invoke() },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            AnimatedContent(
                targetState = isLoggedIn,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) {
                if (!it) {
                    WebView(
                        modifier = Modifier.fillMaxSize(),
                        state = webViewState,
                    )
                } else {
                    LoadingView(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

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
