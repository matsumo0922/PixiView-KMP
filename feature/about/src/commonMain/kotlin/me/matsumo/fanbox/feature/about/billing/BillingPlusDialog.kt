package me.matsumo.fanbox.feature.about.billing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.LocalPlatformContext
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.view.LoadingView
import me.matsumo.fanbox.feature.about.billing.items.billingPlusDescriptionSection
import me.matsumo.fanbox.feature.about.billing.items.billingPlusTitleSection
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun BillingPlusRoute(
    referrer: String,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillingPlusViewModel = koinViewModel(),
    toastExtension: ToastExtension = koinInject()
) {
    val context = LocalPlatformContext.current
    val snackbarHostState = LocalSnackbarHostState.current

    val scope = rememberCoroutineScope()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = {
            terminate.invoke()
        },
    ) { uiState ->
        BillingPlusDialog(
            modifier = Modifier.fillMaxSize(),
            plans = uiState.plans.toImmutableList(),
            isDeveloperMode = uiState.isDeveloperMode,
            isLoading = isLoading,
            onClickPurchase = {
                scope.launch {
                    isLoading = true

                    val isSuccess = viewModel.purchase(context)

                    BillingLog.purchase(
                        referrer = referrer,
                        isSuccess = isSuccess,
                    ).send()

                    if (isSuccess) {
                        isLoading = false
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_purchased)
                        terminate.invoke()
                    } else {
                        isLoading = false
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_purchased_error)
                    }
                }
            },
            onClickVerify = {
                scope.launch {
                    val isSuccess = viewModel.verify(context)

                    BillingLog.verify(isSuccess).send()

                    if (isSuccess) {
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_verify)
                        terminate.invoke()
                    } else {
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_verify_error)
                    }
                }
            },
            onClickConsume = {
                scope.launch {
                    val isSuccess = viewModel.consume(context)

                    BillingLog.consume(isSuccess).send()

                    if (isSuccess) {
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_consumed)
                    } else {
                        toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_consumed_error)
                    }
                }
            },
            onTerminate = terminate,
        )

        LaunchedEffect(uiState.isPlusMode) {
            if (uiState.isPlusMode) {
                toastExtension.show(snackbarHostState, MR.strings.billing_plus_toast_purchased)

                if (!uiState.isDeveloperMode) {
                    terminate.invoke()
                }
            }
        }
    }
}

@Composable
private fun BillingPlusDialog(
    plans: ImmutableList<BillingPlusUiState.Plan>,
    isLoading: Boolean,
    isDeveloperMode: Boolean,
    onClickPurchase: () -> Unit,
    onClickVerify: () -> Unit,
    onClickConsume: () -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            billingPlusTitleSection(onTerminate)
            billingPlusDescriptionSection()
        }


    }

    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TitleItem(
                modifier = Modifier.fillMaxWidth(),
                onTerminate = onTerminate
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    text = stringResource(MR.strings.billing_plus_description, appName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Button(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    onClick = { onClickPurchase.invoke() },
                ) {
                    Text(stringResource(MR.strings.billing_plus_purchase_button, formattedPrice ?: "￥300"))
                }

                OutlinedButton(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    onClick = { onClickVerify.invoke() },
                ) {
                    Text(stringResource(MR.strings.billing_plus_verify_button))
                }

                if (isDeveloperMode) {
                    OutlinedButton(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        onClick = { onClickConsume.invoke() },
                    ) {
                        Text(stringResource(MR.strings.billing_plus_consume_button))
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 24.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = stringResource(MR.strings.billing_plus_caution1, appName),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = stringResource(MR.strings.billing_plus_caution2),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(8.dp))


                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            )
        }
    }
}
