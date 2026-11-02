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
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.logs.category.WelcomeLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.LoginState
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.welcome_login_title
import me.matsumo.fanbox.core.resources.welcome_login_web_help
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.feature.welcome.LoginCheckTrigger
import me.matsumo.fanbox.feature.welcome.loginFailureMessage
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
    val fanboxOrigin = "https://www.fanbox.cc"
    val fanboxUrl = "$fanboxOrigin/login"
    val fanboxRedirectUrl = "$fanboxOrigin/creators/find"

    val webViewState = rememberWebViewState("$fanboxUrl?return_to=$fanboxRedirectUrl")
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val currentCookies = remember { mutableStateListOf<Cookie>() }
    var isDisplayHelpDialog by remember { mutableStateOf(false) }
    var checkedSessionId by remember { mutableStateOf<String?>(null) }
    var isLoggedIn by remember { mutableStateOf(false) }

    suspend fun tryLogin(trigger: LoginCheckTrigger) {
        val loginState = viewModel.checkSessionId(currentCookies, trigger)

        if (loginState is LoginState.LoggedIn && viewModel.saveCookies(currentCookies)) {
            isLoggedIn = true
            terminate.invoke()
        } else {
            snackExtension.show(
                snackbarHostState = snackbarHostState,
                message = getString(loginState.loginFailureMessage()),
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

    // どの画面で利用者が諦めたかを数える。認証画面で止まったのか、ログイン後の FANBOX まで戻れなかったのかを分けるため。
    DisposableEffect(Unit) {
        onDispose {
            WelcomeLog.webLoginClosed(
                lastHost = webViewState.lastLoadedUrl?.urlHost().orEmpty(),
                isLoggedIn = isLoggedIn,
            ).send()
        }
    }

    // ページを読み込むたびに Cookie を取り直し、ログインが終わっていれば確かめに行く。
    suspend fun onPageLoaded(url: String?) {
        // FANBOX オリジンの Cookie だけを保持する。ログインは oauth.secure.pixiv.net を経由するが、
        // そのオリジンの Cookie は FANBOX のリクエストに不要であり、保存すると以降のすべての
        // リクエストに送信され続ける。
        val fanboxCookies = webViewState.cookieManager.getCookies(fanboxOrigin)

        currentCookies.clear()
        currentCookies.addAll(fanboxCookies)

        val sessionId = currentCookies.find { it.name == "FANBOXSESSID" }?.value
        val shouldCheck = shouldCheckWebLogin(
            url = url,
            redirectUrl = fanboxRedirectUrl,
            sessionId = sessionId,
            checkedSessionId = checkedSessionId,
        )

        if (!shouldCheck) return

        val trigger = if (url == fanboxRedirectUrl) LoginCheckTrigger.Redirect else LoginCheckTrigger.FanboxPage

        // Cookie の値は認証情報そのものであり、logcat に残すと他経路から読み取られうるため出力しない。
        Napier.d { "try login. current url: $url, cookies: ${currentCookies.map { it.name }}" }

        checkedSessionId = sessionId
        tryLogin(trigger)
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        onPageLoaded(webViewState.lastLoadedUrl)
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
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(Res.string.welcome_login_web_help),
                    color = MaterialTheme.colorScheme.onPrimary,
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
            currentCookies = currentCookies.map { "${it.name}=${it.value}" }.toImmutableList(),
            onDismissRequest = { isDisplayHelpDialog = false },
            onClickLogin = {
                scope.launch {
                    isDisplayHelpDialog = false
                    tryLogin(LoginCheckTrigger.Help)
                }
            },
        )
    }
}
