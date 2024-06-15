package me.matsumo.fanbox.feature.welcome.top

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.feature.welcome.WelcomeIndicatorItem
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun WelcomeTopScreen(
    navigateToWelcomeLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeTopViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val navigationType = LocalNavigationType.current.type

    if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) {
        Column(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            FirstSection()

            SecondSection(
                modifier = Modifier.weight(1f),
                navigateToWelcomeLogin = navigateToWelcomeLogin,
                setAgreedPrivacyPolicy = viewModel::setAgreedPrivacyPolicy,
                setAgreedTermsOfService = viewModel::setAgreedTermsOfService,
                navigateToWebPage = { navigatorExtension.navigateToWebPage(it, WelcomeTopRoute) },
            )
        }
    } else {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FirstSection(
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column(Modifier.weight(1f)) {
                Box(Modifier.weight(2f))

                SecondSection(
                    modifier = Modifier.weight(3f),
                    navigateToWelcomeLogin = navigateToWelcomeLogin,
                    setAgreedPrivacyPolicy = viewModel::setAgreedPrivacyPolicy,
                    setAgreedTermsOfService = viewModel::setAgreedTermsOfService,
                    navigateToWebPage = { navigatorExtension.navigateToWebPage(it, WelcomeTopRoute) },
                )
            }
        }
    }
}

@Composable
private fun FirstSection(
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Image(
            modifier = Modifier
                .padding(80.dp, 40.dp)
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            painter = painterResource(MR.images.ic_app_icon),
            contentDescription = null,
        )
    }
}

@Composable
private fun SecondSection(
    navigateToWelcomeLogin: () -> Unit,
    navigateToWebPage: (String) -> Unit,
    setAgreedPrivacyPolicy: suspend () -> Unit,
    setAgreedTermsOfService: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    var isAgreedPrivacyPolicy by remember { mutableStateOf(false) }
    var isAgreedTermsOfService by remember { mutableStateOf(false) }

    val teamOfServiceUri = "https://www.matsumo.me/application/pixiview/team_of_service"
    val privacyPolicyUri = "https://www.matsumo.me/application/pixiview/privacy_policy"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = appName,
            style = MaterialTheme.typography.displaySmall.bold().center(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            text = stringResource(MR.strings.welcome_description),
            style = MaterialTheme.typography.bodySmall.center(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CheckBoxLinkButton(
                isChecked = isAgreedTermsOfService,
                link = stringResource(MR.strings.welcome_team_of_service),
                body = stringResource(MR.strings.welcome_agree, stringResource(MR.strings.welcome_team_of_service)),
                onChecked = { isAgreedTermsOfService = it },
                onClickLink = { navigateToWebPage.invoke(teamOfServiceUri) },
            )

            CheckBoxLinkButton(
                isChecked = isAgreedPrivacyPolicy,
                link = stringResource(MR.strings.welcome_privacy_policy),
                body = stringResource(MR.strings.welcome_agree, stringResource(MR.strings.welcome_privacy_policy)),
                onChecked = { isAgreedPrivacyPolicy = it },
                onClickLink = { navigateToWebPage.invoke(privacyPolicyUri) },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        WelcomeIndicatorItem(
            modifier = Modifier.padding(bottom = 24.dp),
            max = 3,
            step = 1,
        )

        Button(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
            shape = CircleShape,
            enabled = isAgreedPrivacyPolicy && isAgreedTermsOfService,
            onClick = {
                scope.launch {
                    setAgreedPrivacyPolicy.invoke()
                    setAgreedTermsOfService.invoke()
                    navigateToWelcomeLogin.invoke()
                }
            },
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(MR.strings.welcome_button_next),
                color = if (isAgreedPrivacyPolicy && isAgreedTermsOfService) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CheckBoxLinkButton(
    isChecked: Boolean,
    link: String,
    body: String,
    onChecked: (Boolean) -> Unit,
    onClickLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startIndex = body.indexOf(link)
    val endIndex = startIndex + link.length

    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
    )

    val annotatedString = buildAnnotatedString {
        withStyle(TextStyle(color = MaterialTheme.colorScheme.onSurface).toSpanStyle()) {
            append(body)
        }

        addStringAnnotation("url", link, startIndex, endIndex)

        addStyle(textStyle.toSpanStyle(), startIndex, endIndex)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            modifier = Modifier.size(12.dp),
            checked = isChecked,
            onCheckedChange = onChecked,
        )

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            onClick = {
                annotatedString.getStringAnnotations("url", it, it).firstOrNull()?.let { _ ->
                    onClickLink.invoke()
                }
            },
        )
    }
}
