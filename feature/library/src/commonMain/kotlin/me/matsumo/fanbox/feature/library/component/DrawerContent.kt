package me.matsumo.fanbox.feature.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.im_default_user
import me.matsumo.fanbox.core.resources.library_navigation_about
import me.matsumo.fanbox.core.resources.library_navigation_bookmark
import me.matsumo.fanbox.core.resources.library_navigation_discovery
import me.matsumo.fanbox.core.resources.library_navigation_following
import me.matsumo.fanbox.core.resources.library_navigation_home
import me.matsumo.fanbox.core.resources.library_navigation_message
import me.matsumo.fanbox.core.resources.library_navigation_notify
import me.matsumo.fanbox.core.resources.library_navigation_payments
import me.matsumo.fanbox.core.resources.library_navigation_plus_description
import me.matsumo.fanbox.core.resources.library_navigation_plus_purchased_description
import me.matsumo.fanbox.core.resources.library_navigation_queue
import me.matsumo.fanbox.core.resources.library_navigation_setting
import me.matsumo.fanbox.core.resources.library_navigation_supporting
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@Composable
fun DrawerContent(
    state: DrawerState?,
    setting: Setting?,
    currentDestination: NavDestination?,
    onClickLibrary: (LibraryDestination) -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(256.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        NavigationDrawerHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .statusBarsPadding(),
        )

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                modifier = Modifier.padding(top = 8.dp),
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Home),
                label = stringResource(Res.string.library_navigation_home),
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                onClick = { onClickLibrary.invoke(LibraryDestination.Home) },
            )
        }

        NavigationDrawerItem(
            state = state,
            isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Discovery),
            label = stringResource(Res.string.library_navigation_discovery),
            icon = Icons.Outlined.Search,
            selectedIcon = Icons.Default.Search,
            onClick = { onClickLibrary.invoke(LibraryDestination.Discovery) },
        )

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Notify),
                label = stringResource(Res.string.library_navigation_notify),
                icon = Icons.Outlined.Notifications,
                selectedIcon = Icons.Default.Notifications,
                onClick = { onClickLibrary.invoke(LibraryDestination.Notify) },
            )
        }

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Message),
                label = stringResource(Res.string.library_navigation_message),
                icon = Icons.Outlined.Mail,
                selectedIcon = Icons.Default.Mail,
                onClick = { onClickLibrary.invoke(LibraryDestination.Message) },
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
        )

        NavigationDrawerItem(
            state = state,
            label = stringResource(Res.string.library_navigation_bookmark),
            icon = Icons.Outlined.Bookmark,
            onClick = { navigateTo(Destination.BookmarkedPosts) },
        )

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                state = state,
                label = stringResource(Res.string.library_navigation_following),
                icon = Icons.Outlined.PersonAdd,
                onClick = { navigateTo(Destination.FollowingCreators) },
            )
        }

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                state = state,
                label = stringResource(Res.string.library_navigation_supporting),
                icon = Icons.Outlined.Group,
                onClick = { navigateTo(Destination.SupportingCreators) },
            )
        }

        if (setting?.isTestUser == false) {
            NavigationDrawerItem(
                state = state,
                label = stringResource(Res.string.library_navigation_payments),
                icon = Icons.Outlined.Payment,
                onClick = { navigateTo(Destination.Payments) },
            )
        }

        NavigationDrawerItem(
            state = state,
            label = stringResource(Res.string.library_navigation_queue),
            icon = Icons.Filled.Download,
            onClick = { navigateTo(Destination.DownloadQueue) },
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
        )

        NavigationDrawerItem(
            state = state,
            label = stringResource(Res.string.library_navigation_setting),
            icon = Icons.Default.Settings,
            onClick = { navigateTo(Destination.SettingTop) },
        )

        NavigationDrawerItem(
            state = state,
            label = stringResource(Res.string.library_navigation_about),
            icon = Icons.Outlined.Info,
            onClick = { navigateTo(Destination.About) },
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
        )

        NavigationDrawerPlusItem(
            state = state,
            isPlusMode = setting?.isPlusMode == true,
            isDeveloperMode = setting?.isDeveloperMode == true,
            onClick = { navigateTo(Destination.BillingPlusBottomSheet("drawer")) },
        )
    }
}

@Composable
private fun NavigationDrawerHeader(modifier: Modifier = Modifier) {
    val metadata = LocalFanboxMetadata.current

    Row(
        modifier = modifier.padding(16.dp, 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(metadata.context?.user?.iconUrl)
                .build(),
            loading = {
                FadePlaceHolder()
            },
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = metadata.context?.user?.name.takeIf { !it.isNullOrBlank() } ?: "Unknown User",
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "@${metadata.context?.user?.userId ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NavigationDrawerItem(
    state: DrawerState?,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectedIcon: ImageVector = icon,
) {
    val scope = rememberCoroutineScope()
    val containerColor: Color
    val contentColor: Color

    if (isSelected) {
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        contentColor = MaterialTheme.colorScheme.primary
    } else {
        containerColor = Color.Transparent
        contentColor = MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .padding(end = 16.dp)
            .clip(
                RoundedCornerShape(
                    topEnd = 32.dp,
                    bottomEnd = 32.dp,
                ),
            )
            .background(containerColor)
            .clickable {
                scope.launch {
                    state?.close()
                    onClick.invoke()
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = null,
            tint = contentColor,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun NavigationDrawerPlusItem(
    isPlusMode: Boolean,
    isDeveloperMode: Boolean,
    state: DrawerState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val titleStyle = MaterialTheme.typography.titleMedium.bold()
    val title: AnnotatedString
    val description: String

    if (isPlusMode) {
        title = buildAnnotatedString {
            withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
                append("$appName+")
            }
        }
        description = stringResource(Res.string.library_navigation_plus_purchased_description, appName)
    } else {
        title = buildAnnotatedString {
            append("Buy ")
            withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
                append("Plus+")
            }
        }
        description = stringResource(Res.string.library_navigation_plus_description)
    }

    Row(
        modifier = modifier
            .clickable {
                scope.launch {
                    if (!isPlusMode || isDeveloperMode) {
                        state?.close()
                        onClick.invoke()
                    }
                }
            }
            .padding(top = 8.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
