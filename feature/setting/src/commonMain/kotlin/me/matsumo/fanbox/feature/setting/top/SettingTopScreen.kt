package me.matsumo.fanbox.feature.setting.top

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.biometry.compose.BindBiometryAuthenticatorEffect
import dev.icerock.moko.biometry.compose.rememberBiometryAuthenticatorFactory
import dev.icerock.moko.resources.compose.stringResource
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveScaffold
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.setting.SettingTheme
import me.matsumo.fanbox.feature.setting.top.items.SettingTopAccountSection
import me.matsumo.fanbox.feature.setting.top.items.SettingTopGeneralSection
import me.matsumo.fanbox.feature.setting.top.items.SettingTopInformationSection
import me.matsumo.fanbox.feature.setting.top.items.SettingTopOthersSection
import me.matsumo.fanbox.feature.setting.top.items.SettingTopThemeSection
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingTopRoute(
    navigateToThemeSetting: () -> Unit,
    navigateToBillingPlus: () -> Unit,
    navigateToLogoutDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToOpenSourceLicense: () -> Unit,
    navigateToSettingDeveloper: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingTopViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    val scope = rememberCoroutineScope()

    val biometryAuthenticatorFactory = rememberBiometryAuthenticatorFactory()
    val biometryAuthenticator = biometryAuthenticatorFactory.createBiometryAuthenticator()

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val requirePlus = stringResource(MR.strings.billing_plus_toast_require_plus, appName)

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) { uiState ->
        SettingTopScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            userData = uiState.userData,
            metaData = uiState.metaData,
            fanboxSessionId = uiState.fanboxSessionId,
            config = uiState.config,
            onClickThemeSetting = navigateToThemeSetting,
            onClickAccountSetting = { navigatorExtension.navigateToWebPage("https://www.fanbox.cc/user/settings") },
            onClickNotifySetting = { navigatorExtension.navigateToWebPage("https://www.fanbox.cc/notifications/settings") },
            onClickTeamsOfService = { navigatorExtension.navigateToWebPage("https://www.matsumo.me/application/pixiview/team_of_service") },
            onClickPrivacyPolicy = { navigatorExtension.navigateToWebPage("https://www.matsumo.me/application/pixiview/privacy_policy") },
            onClickOpenSourceLicense = navigateToOpenSourceLicense,
            onClickFollowTabDefaultHome = viewModel::setFollowTabDefaultHome,
            onClickHideAdultContents = viewModel::setHideAdultContents,
            onClickOverrideAdultContents = viewModel::setOverrideAdultContents,
            onClickInfinityPostDetail = viewModel::setUseInfinityPostDetail,
            onClickGridMode = {
                if (it) {
                    if (uiState.userData.hasPrivilege) {
                        viewModel.setGridMode(true)
                    } else {
                        scope.launch { snackbarExtension.showSnackbar(snackbarHostState, requirePlus) }
                        navigateToBillingPlus.invoke()
                    }
                } else {
                    viewModel.setGridMode(false)
                }
            },
            onClickHideRestricted = {
                if (it) {
                    if (uiState.userData.hasPrivilege) {
                        viewModel.setHideRestricted(true)
                    } else {
                        scope.launch { snackbarExtension.showSnackbar(snackbarHostState, requirePlus) }
                        navigateToBillingPlus.invoke()
                    }
                } else {
                    viewModel.setHideRestricted(false)
                }
            },
            onClickAppLock = {
                if (it) {
                    if (uiState.userData.hasPrivilege) {
                        scope.launch {
                            if (viewModel.tryToAuthenticate(biometryAuthenticator)) {
                                viewModel.setAppLock(true)
                            }
                        }
                    } else {
                        navigateToBillingPlus.invoke()
                    }
                } else {
                    viewModel.setAppLock(false)
                }
            },
            onClickLogout = {
                navigateToLogoutDialog(SimpleAlertContents.Logout) {
                    scope.launch {
                        viewModel.logout().fold(
                            onSuccess = {
                                scope.launch { snackbarExtension.showSnackbar(snackbarHostState, MR.strings.setting_top_others_logout_dialog_success) }
                                terminate.invoke()
                            },
                            onFailure = {
                                scope.launch { snackbarExtension.showSnackbar(snackbarHostState, MR.strings.setting_top_others_logout_dialog_failed) }
                            },
                        )
                    }
                }
            },
            onClickDeveloperMode = { isEnable ->
                if (isEnable) {
                    navigateToSettingDeveloper.invoke()
                } else {
                    viewModel.setDeveloperMode(false)
                }
            },
            onTerminate = terminate,
        )
    }

    BindBiometryAuthenticatorEffect(biometryAuthenticator)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class)
@Composable
private fun SettingTopScreen(
    userData: UserData,
    metaData: FanboxMetaData,
    fanboxSessionId: String,
    config: PixiViewConfig,
    onClickThemeSetting: () -> Unit,
    onClickAccountSetting: () -> Unit,
    onClickNotifySetting: () -> Unit,
    onClickAppLock: (Boolean) -> Unit,
    onClickFollowTabDefaultHome: (Boolean) -> Unit,
    onClickHideAdultContents: (Boolean) -> Unit,
    onClickOverrideAdultContents: (Boolean) -> Unit,
    onClickHideRestricted: (Boolean) -> Unit,
    onClickGridMode: (Boolean) -> Unit,
    onClickInfinityPostDetail: (Boolean) -> Unit,
    onClickTeamsOfService: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
    onClickLogout: () -> Unit,
    onClickOpenSourceLicense: () -> Unit,
    onClickDeveloperMode: (Boolean) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)

    AdaptiveScaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            SettingTheme {
                LargeTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(MR.strings.setting_title),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onTerminate) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    scrollBehavior = behavior,
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
        ) {
            item {
                SettingTopAccountSection(
                    modifier = Modifier.fillMaxWidth(),
                    onClickAccountSetting = onClickAccountSetting,
                    onClickNotifySetting = onClickNotifySetting,
                )
            }

            item {
                SettingTopThemeSection(
                    modifier = Modifier.fillMaxWidth(),
                    onClickAppTheme = onClickThemeSetting,
                )
            }

            item {
                SettingTopGeneralSection(
                    modifier = Modifier.fillMaxWidth(),
                    userData = userData,
                    onClickAppLock = onClickAppLock,
                    onClickFollowTabDefaultHome = onClickFollowTabDefaultHome,
                    onClickHideAdultContents = onClickHideAdultContents,
                    onClickOverrideAdultContents = onClickOverrideAdultContents,
                    onClickHideRestricted = onClickHideRestricted,
                    onClickGridMode = onClickGridMode,
                    onClickInfinityPostDetail = onClickInfinityPostDetail,
                )
            }

            item {
                SettingTopInformationSection(
                    modifier = Modifier.fillMaxWidth(),
                    config = config,
                    userData = userData,
                    fanboxMetaData = metaData,
                    fanboxSessionId = fanboxSessionId,
                )
            }

            item {
                SettingTopOthersSection(
                    modifier = Modifier.fillMaxWidth(),
                    userData = userData,
                    onClickTeamsOfService = onClickTeamsOfService,
                    onClickPrivacyPolicy = onClickPrivacyPolicy,
                    onClickLogout = onClickLogout,
                    onClickOpenSourceLicense = onClickOpenSourceLicense,
                    onClickDeveloperMode = onClickDeveloperMode,
                )
            }
        }
    }
}
