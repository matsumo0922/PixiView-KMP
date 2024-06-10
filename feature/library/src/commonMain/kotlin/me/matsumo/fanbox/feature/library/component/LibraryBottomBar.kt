package me.matsumo.fanbox.feature.library.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import dev.icerock.moko.resources.compose.stringResource
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.ui.component.PixiViewNavigationBar
import me.matsumo.fanbox.core.ui.component.PixiViewNavigationBarItem
import me.matsumo.fanbox.core.ui.component.PixiViewNavigationDefaults

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun LibraryBottomBar(
    destinations: ImmutableList<LibraryDestination>,
    navigateToDestination: (LibraryDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    PixiViewNavigationBar(modifier) {
        destinations.forEach { destination ->
            val isSelected = currentDestination.isLibraryDestinationInHierarchy(destination)

            PixiViewNavigationBarItem(
                isSelected = isSelected,
                onClick = { navigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.deselectedIcon,
                        contentDescription = stringResource(destination.title),
                        tint = if (isSelected) {
                            PixiViewNavigationDefaults.navigationSelectedItemColor()
                        } else {
                            PixiViewNavigationDefaults.navigationContentColor()
                        },
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        }
    }
}
