package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** FANBOX ページのメタデータ。 */
@Immutable
data class MetaData(
    val apiUrl: String?,
    val context: Context?,
    val csrfToken: String,
) {
    /** ページ読み込み時点のコンテキスト情報。 */
    @Immutable
    data class Context(
        val privacyPolicy: PrivacyPolicy,
        val user: User,
    ) {
        /** プライバシーポリシーの掲示情報。 */
        @Immutable
        data class PrivacyPolicy(
            val policyUrl: String,
            val revisionHistoryUrl: String,
            val shouldShowNotice: Boolean,
            val updateDate: String,
        )

        /** ログイン中ユーザーの情報。 */
        @Immutable
        data class User(
            val creatorId: CreatorId?,
            val fanboxUserStatus: Int,
            val hasAdultContent: Boolean,
            val hasUnpaidPayments: Boolean,
            val iconUrl: String?,
            val isCreator: Boolean,
            val isMailAddressOutdated: Boolean,
            val isSupporter: Boolean,
            val lang: String,
            val name: String,
            val planCount: Int,
            val showAdultContent: Boolean,
            val userId: UserId?,
        )
    }
}
