package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** 支援プラン。 */
@Immutable
data class CreatorPlan(
    val id: PlanId,
    val title: String,
    val description: String,
    val fee: Int,
    val coverImageUrl: String?,
    val hasAdultContent: Boolean,
    val paymentMethod: PaymentMethod,
    val user: User?,
) {
    /** プラン詳細画面の URL。 */
    val planBrowserUrl get() = "https://www.fanbox.cc/@${user?.creatorId?.value}/plans/${id.value}"

    /** クリエイターサポート画面の URL。 */
    val supportingBrowserUrl get() = "https://www.fanbox.cc/creators/supporting/@${user?.creatorId?.value}"
}
