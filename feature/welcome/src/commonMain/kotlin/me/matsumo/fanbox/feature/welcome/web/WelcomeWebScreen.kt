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
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.core.ui.welcome_login_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
internal fun WelcomeWebScreen(
    navigateToLoginAlert: (SimpleAlertContents) -> Unit,
    navigateToLoginDebugAlert: (SimpleAlertContents, () -> Unit) -> Unit,
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
        if (webViewState.lastLoadedUrl == fanboxRedirectUrl) {
            val oauthCookies = webViewState.cookieManager.getCookies("https://oauth.secure.pixiv.net").associate { it.name to it.value }
            val fanboxCookies = webViewState.cookieManager.getCookies("https://www.fanbox.cc").associate { it.name to it.value }
            val cookieString = (fanboxCookies + oauthCookies)
                .filterKeys { listOf("__cf_bm", "cf_clearance", "FANBOXSESSID").contains(it) }
                .map { "${it.key}=${it.value}" }
                .joinToString(";")

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
                actionsIcon = null,
                onClickNavigation = { terminate.invoke() },
                onClickActions = {
                    navigateToLoginDebugAlert.invoke(SimpleAlertContents.LoginDebug) {
                        viewModel.debugLogin()
                        terminate.invoke()
                    }
                }
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
