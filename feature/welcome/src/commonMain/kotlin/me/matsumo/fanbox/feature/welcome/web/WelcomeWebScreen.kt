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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WelcomeWebScreen(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeWebViewModel = koinViewModel(WelcomeWebViewModel::class),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    val fanboxUrl = "https://www.fanbox.cc/login"
    val fanboxRedirectUrl = "https://www.fanbox.cc/creators/find"

    val webViewState = rememberWebViewState("$fanboxUrl?return_to=$fanboxRedirectUrl")
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

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

            scope.launch {
                snackbarExtension.showSnackbar(snackbarHostState, MR.strings.welcome_login_toast_success)
            }

            viewModel.saveCookie(cookieString)
            terminate.invoke()
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
