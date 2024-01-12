package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
internal fun PostDetailBottomSection(
    onClickWebBrowser: () -> Unit,
    onClickShare: () -> Unit,
    onClickPlans: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        Icon(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                )
                .clickable { onClickWebBrowser.invoke() }
                .padding(8.dp),
            imageVector = Icons.Default.Language,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )

        Icon(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                )
                .clickable { onClickShare.invoke() }
                .padding(8.dp),
            imageVector = Icons.Default.Share,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )

        Icon(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                )
                .clickable { onClickPlans.invoke() }
                .padding(8.dp),
            imageVector = Icons.Outlined.Paid,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )
    }
}
