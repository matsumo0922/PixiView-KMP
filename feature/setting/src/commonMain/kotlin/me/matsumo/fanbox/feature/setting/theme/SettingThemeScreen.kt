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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_toast_require_plus
import me.matsumo.fanbox.core.resources.setting_theme_theme_dynamic_color
import me.matsumo.fanbox.core.resources.setting_theme_theme_dynamic_color_description
import me.matsumo.fanbox.core.resources.setting_theme_title
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.feature.setting.SettingTheme
import me.matsumo.fanbox.feature.setting.theme.items.SettingThemeColorSection
import me.matsumo.fanbox.feature.setting.theme.items.SettingThemeTabsSection
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingThemeRoute(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingThemeViewModel = koinViewModel(),
    toastExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        terminate = { terminate.invoke() },
    ) {
        SettingThemeDialog(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            setting = it.setting,
            onClickBillingPlus = { navigateTo(Destination.BillingPlusBottomSheet("")) },
            onSelectTheme = viewModel::setThemeConfig,
            onSelectThemeColor = viewModel::setThemeColorConfig,
            onClickDynamicColor = viewModel::setUseDynamicColor,
            onShowSnackbar = { scope.launch { toastExtension.show(snackbarHostState, it) } },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingThemeDialog(
    setting: Setting,
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
                            text = stringResource(Res.string.setting_theme_title),
                        )
                    },
                    navigationIcon = {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                                .clickable { onTerminate.invoke() },
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                    themeConfig = setting.themeConfig,
                    onSelectTheme = onSelectTheme,
                )
            }

            item {
                SettingSwitchItem(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    title = Res.string.setting_theme_theme_dynamic_color,
                    description = Res.string.setting_theme_theme_dynamic_color_description,
                    isEnabled = currentPlatform == Platform.Android,
                    value = if (currentPlatform == Platform.Android) setting.isUseDynamicColor else false,
                    onValueChanged = {
                        if (setting.hasPrivilege) {
                            onClickDynamicColor.invoke(it)
                        } else {
                            onShowSnackbar.invoke(Res.string.billing_plus_toast_require_plus)
                            onClickBillingPlus.invoke()
                        }
                    },
                )
            }

            item {
                SettingThemeColorSection(
                    modifier = Modifier.fillMaxWidth(),
                    isUseDynamicColor = setting.isUseDynamicColor,
                    themeConfig = setting.themeConfig,
                    themeColorConfig = setting.themeColorConfig,
                    onSelectThemeColor = onSelectThemeColor,
                )
            }
        }
    }
}
