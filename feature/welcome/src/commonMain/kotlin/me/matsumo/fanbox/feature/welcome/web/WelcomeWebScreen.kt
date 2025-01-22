package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.cookie.Cookie
import com.multiplatform.webview.cookie.WebViewCookieManager
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.welcome_login_title
import me.matsumo.fanbox.core.resources.welcome_login_toast_failed
import me.matsumo.fanbox.core.resources.welcome_login_web_help
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WelcomeWebScreen(
    navigateToLoginAlert: (SimpleAlertContents) -> Unit,
    navigateToLoginDebugAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeWebViewModel = koinViewModel(),
    snackExtension: ToastExtension = koinInject(),
) {
    val fanboxUrl = "https://www.fanbox.cc/login"
    val fanboxRedirectUrl = "https://www.fanbox.cc/creators/find"

    val webViewState = rememberWebViewState("$fanboxUrl?return_to=$fanboxRedirectUrl")
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val currentCookies = remember { mutableStateListOf<Cookie>() }
    var isDisplayHelpDialog by remember { mutableStateOf(false) }

    suspend fun tryLogin() {
        val sessionId = currentCookies.find { it.name == "FANBOXSESSID" }

        if (sessionId != null && viewModel.checkSessionId(sessionId.value)) {
            viewModel.saveSessionId(sessionId.value)
            terminate.invoke()
        } else {
            snackExtension.show(
                snackbarHostState = snackbarHostState,
                message = getString(Res.string.welcome_login_toast_failed),
                isSnackbar = true,
            )
        }
    }

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
        withContext(Dispatchers.Main) { WebViewCookieManager().removeAllCookies() }
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        val oauthCookies = webViewState.cookieManager.getCookies("https://oauth.secure.pixiv.net")
        val fanboxCookies = webViewState.cookieManager.getCookies("https://www.fanbox.cc")

        currentCookies.clear()
        currentCookies.addAll(fanboxCookies + oauthCookies)

        Napier.d { "WebView current url: ${webViewState.lastLoadedUrl} == $fanboxRedirectUrl" }

        if (webViewState.lastLoadedUrl == fanboxRedirectUrl) {
            tryLogin()
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
                },
            )
        },
        bottomBar = {
            HorizontalDivider()

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                onClick = { isDisplayHelpDialog = true },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(Res.string.welcome_login_web_help),
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
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
                    progress = { it.progress },
                )
            }
        }
    }

    if (isDisplayHelpDialog) {
        WelcomeWebDialog(
            currentUrl = webViewState.lastLoadedUrl.orEmpty(),
            currentCookies = currentCookies.map { "${it.name}=${it.value}" },
            onDismissRequest = { isDisplayHelpDialog = false },
            onClickLogin = {
                scope.launch {
                    isDisplayHelpDialog = false
                    tryLogin()
                }
            },
        )
    }
}
