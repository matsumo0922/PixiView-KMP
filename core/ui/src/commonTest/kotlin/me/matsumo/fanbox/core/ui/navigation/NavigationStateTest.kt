package me.matsumo.fanbox.core.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** バックスタックの保存と復元、遷移先ごとの識別を検証する。 */
class NavigationStateTest {

    @Test
    fun restoresBackStackOrderAndDestinationArguments() {
        val backStack = NavBackStack(
            DestinationKey.of(Destination.Library),
            DestinationKey.of(Destination.PostDetail(PostId("1"), Destination.PostDetail.PagingType.Creator)),
            DestinationKey.of(Destination.CreatorTop(CreatorId("creator"), isPosts = false)),
            DestinationKey.of(Destination.PostSearch(CreatorId("creator"), creatorQuery = null, tag = "tag")),
            DestinationKey.of(Destination.SimpleAlertDialog(SimpleAlertContents.CommentDelete)),
            DestinationKey.of(Destination.BillingPlusBottomSheet("drawer", BillingPlan.Type.ANNUAL)),
            DestinationKey.of(Destination.BillingRetentionBottomSheet(isAnnualOfferShown = true)),
        )
        val serializer = NavBackStackSerializer(DestinationKey.serializer())

        val savedState = Json.encodeToString(serializer, backStack)
        val restored = Json.decodeFromString(serializer, savedState)

        assertEquals(backStack.toList(), restored.toList())
    }

    @Test
    fun sameDestinationPushedTwiceGetsDistinctKeys() {
        val destination = Destination.PostDetail(PostId("1"))

        val first = DestinationKey.of(destination)
        val second = DestinationKey.of(destination)

        assertNotEquals(first.id, second.id)
    }
}
