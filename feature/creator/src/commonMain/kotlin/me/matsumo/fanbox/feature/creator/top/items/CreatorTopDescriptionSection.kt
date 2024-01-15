package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.ui.component.AutoLinkText

@Composable
internal fun CreatorTopDescriptionSection(
    creatorDetail: FanboxCreatorDetail,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val descriptionFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val descriptionFontColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        AutoLinkText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 16.dp),
            text = creatorDetail.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            onClickLink = onLinkClick,
        )
    }
}
