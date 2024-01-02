package core.model.fanbox

import core.model.fanbox.id.PlanId


data class FanboxCreatorPlan(
    val id: PlanId,
    val title: String,
    val description: String,
    val fee: Int,
    val coverImageUrl: String?,
    val hasAdultContent: Boolean,
    val paymentMethod: PaymentMethod,
    val user: FanboxUser,
) {
    val planBrowserUrl get() = "https://www.fanbox.cc/@${user.creatorId}/plans/$id"
    val supportingBrowserUrl get() = "https://www.fanbox.cc/creators/supporting/@${user.creatorId}"

    companion object {
        fun dummy() = FanboxCreatorPlan(
            id = PlanId("123"),
            title = "定食プラン",
            description = "PSDファイルをダウンロードできます。（2019年4/30以降にアップロードしたもの）\n高画質のMP4ファイルもあります。\n動画は声優様に依頼した音声付きもあります！",
            fee = 500,
            coverImageUrl = null,
            hasAdultContent = false,
            paymentMethod = PaymentMethod.CARD,
            user = FanboxUser.dummy(),
        )
    }
}
