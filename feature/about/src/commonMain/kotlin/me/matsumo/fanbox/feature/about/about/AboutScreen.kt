package me.matsumo.fanbox.feature.about.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.feature.about.about.items.AboutAppSection
import me.matsumo.fanbox.feature.about.about.items.AboutDeveloperSection
import me.matsumo.fanbox.feature.about.about.items.AboutSupportSection
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun AboutRoute(
    navigateToVersionHistory: () -> Unit,
    navigateToDonate: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = koinViewModel(AboutViewModel::class),
    navigatorExtension: NavigatorExtension = koinInject(),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) { uiState ->
        AboutScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            userData = uiState.userData,
            config = uiState.config,
            onClickGithub = { navigatorExtension.navigateToWebPage("https://github.com/matsumo0922/PixiView") },
            onClickGithubProfile = { navigatorExtension.navigateToWebPage("https://github.com/matsumo0922") },
            onClickGithubIssue = { navigatorExtension.navigateToWebPage("https://github.com/matsumo0922/PixiView/issues/new") },
            onClickGitHubContributor = { navigatorExtension.navigateToWebPage("https://github.com/matsumo0922/PixiView/graphs/contributors") },
            onClickDiscord = { scope.launch { snackbarExtension.showSnackbar(snackHostState, MR.strings.error_developing_feature) } },
            onClickGooglePlay = { navigatorExtension.navigateToWebPage("https://play.google.com/store/apps/details?id=caios.android.fanbox") },
            onClickGooglePlayDeveloper = { navigatorExtension.navigateToWebPage("https://play.google.com/store/apps/developer?id=CAIOS") },
            onClickAppStore = { navigatorExtension.navigateToWebPage("https://apps.apple.com/jp/developer/caios/id1563407383") },
            onClickAppStoreDeveloper = { navigatorExtension.navigateToWebPage("https://apps.apple.com/jp/developer/caios/id1563407383") },
            onClickTwitter = { navigatorExtension.navigateToWebPage("https://twitter.com/matsumo0922") },
            onClickVersionHistory = { navigateToVersionHistory.invoke() },
            onClickDonate = { navigateToDonate.invoke() },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(
    userData: UserData,
    config: PixiViewConfig,
    onClickGithub: () -> Unit,
    onClickGithubProfile: () -> Unit,
    onClickGithubIssue: () -> Unit,
    onClickGitHubContributor: () -> Unit,
    onClickDiscord: () -> Unit,
    onClickAppStore: () -> Unit,
    onClickGooglePlay: () -> Unit,
    onClickAppStoreDeveloper: () -> Unit,
    onClickGooglePlayDeveloper: () -> Unit,
    onClickTwitter: () -> Unit,
    onClickVersionHistory: () -> Unit,
    onClickDonate: () -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(state)

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(MR.strings.about_title),
                onClickNavigation = onTerminate,
                scrollBehavior = behavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = 24.dp + paddingValues.calculateTopPadding(),
                bottom = 24.dp + paddingValues.calculateBottomPadding(),
                start = 24.dp,
                end = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AboutAppSection(
                    modifier = Modifier.fillMaxWidth(),
                    userData = userData,
                    config = config,
                    onClickGithub = onClickGithub,
                    onClickDiscord = onClickDiscord,
                    onClickStore = if (currentPlatform == Platform.IOS) onClickAppStore else onClickGooglePlay,
                )
            }

            item {
                AboutDeveloperSection(
                    modifier = Modifier.fillMaxWidth(),
                    onClickTwitter = onClickTwitter,
                    onClickGithub = onClickGithubProfile,
                    onClickGooglePlay = onClickGooglePlayDeveloper,
                    onClickAppStore = onClickAppStoreDeveloper,
                    onClickGitHubContributor = onClickGitHubContributor,
                )
            }

            item {
                AboutSupportSection(
                    modifier = Modifier.fillMaxWidth(),
                    onClickVersionHistory = onClickVersionHistory,
                    onClickRateApp = if (currentPlatform == Platform.IOS) onClickAppStore else onClickGooglePlay,
                    onClickEmail = onClickGithubIssue,
                    onClickDonation = onClickDonate,
                )
            }
        }
    }
}
