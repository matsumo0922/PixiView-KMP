package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.feature.setting.SettingSwitchItem
import me.matsumo.fanbox.feature.setting.SettingTheme
import me.matsumo.fanbox.feature.setting.theme.items.SettingThemeColorSection
import me.matsumo.fanbox.feature.setting.theme.items.SettingThemeTabsSection
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun SettingThemeRoute(
    navigateToBillingPlus: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingThemeViewModel = koinViewModel(SettingThemeViewModel::class),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        SettingThemeDialog(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            userData = it.userData,
            onClickBillingPlus = navigateToBillingPlus,
            onSelectTheme = viewModel::setThemeConfig,
            onSelectThemeColor = viewModel::setThemeColorConfig,
            onClickDynamicColor = viewModel::setUseDynamicColor,
            onShowSnackbar = { scope.launch { snackbarExtension.showSnackbar(snackbarHostState, it) } },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingThemeDialog(
    userData: UserData,
    onClickBillingPlus: () -> Unit,
    onSelectTheme: (ThemeConfig) -> Unit,
    onSelectThemeColor: (ThemeColorConfig) -> Unit,
    onClickDynamicColor: (Boolean) -> Unit,
    onShowSnackbar: (StringResource) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            SettingTheme {
                LargeTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(MR.strings.setting_theme_title),
                        )
                    },
                    navigationIcon = {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                                .clickable { onTerminate.invoke() },
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                SettingThemeTabsSection(
                    modifier = Modifier.fillMaxWidth(),
                    themeConfig = userData.themeConfig,
                    onSelectTheme = onSelectTheme,
                )
            }

            item {
                SettingSwitchItem(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    title = MR.strings.setting_theme_theme_dynamic_color,
                    description = MR.strings.setting_theme_theme_dynamic_color_description,
                    value = userData.isDynamicColor,
                    onValueChanged = {
                        if (userData.hasPrivilege) {
                            onClickDynamicColor.invoke(it)
                        } else {
                            onShowSnackbar.invoke(MR.strings.billing_plus_toast_require_plus)
                            onClickBillingPlus.invoke()
                        }
                    },
                )
            }

            item {
                SettingThemeColorSection(
                    modifier = Modifier.fillMaxWidth(),
                    isUseDynamicColor = userData.isDynamicColor,
                    themeConfig = userData.themeConfig,
                    themeColorConfig = userData.themeColorConfig,
                    onSelectThemeColor = onSelectThemeColor,
                )
            }
        }
    }
}
