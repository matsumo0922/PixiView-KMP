package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan

@Composable
internal fun CreatorTopPlansScreen(
    state: LazyListState,
    creatorPlans: ImmutableList<FanboxCreatorPlan>,
    onClickPlan: (FanboxCreatorPlan) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CreatorTopPlansSection(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                creatorPlans = creatorPlans,
                onClickPlan = onClickPlan,
            )
        }

        item {
            Spacer(modifier = Modifier.height(128.dp))
        }
    }
}
