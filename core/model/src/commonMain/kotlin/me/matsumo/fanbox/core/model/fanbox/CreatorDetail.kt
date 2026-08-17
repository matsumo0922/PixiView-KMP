package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** クリエイターの詳細情報。 */
@Immutable
data class CreatorDetail(
    val creatorId: CreatorId,
    val coverImageUrl: String?,
    val description: String,
    val hasAdultContent: Boolean,
    val hasBoothShop: Boolean,
    val isAcceptingRequest: Boolean,
    val isFollowed: Boolean,
    val isStopped: Boolean,
    val isSupported: Boolean,
    val profileItems: List<ProfileItem>,
    val profileLinks: List<ProfileLink>,
    val user: User?,
) {
    /** クリエイターサポート画面の URL。 */
    val supportingBrowserUrl get() = "https://www.fanbox.cc/creators/supporting/@${user?.creatorId?.value}"

    /** `creator.get` のアイテム種別から分類したプロフィールアイテム。 */
    @Immutable
    sealed interface ProfileItem {

        /** 画像のプロフィールアイテム。両 URL は FANBOX レスポンスの null 許容値をそのまま保持する。 */
        @Immutable
        data class Image(
            val id: String,
            val imageUrl: String?,
            val thumbnailUrl: String?,
        ) : ProfileItem

        /**
         * 動画のプロフィールアイテム。
         *
         * [url] は既知の YouTube / Vimeo の URL 形式のみを再構築する。[serviceProvider] / [videoId] /
         * 生成される URL は未検証のネットワークデータのまま残る。呼び出し側はナビゲーションポリシーに
         * 照らして検証してから URL を開くこと。
         */
        @Immutable
        data class Video(
            val id: String,
            val serviceProvider: String,
            val videoId: String,
            val thumbnailUrl: String?,
        ) : ProfileItem {
            /** 対応済みサービス提供者の動画 URL。未対応の場合は null。 */
            val url: String?
                get() = when (serviceProvider) {
                    "youtube" -> "https://www.youtube.com/watch?v=$videoId"
                    "vimeo" -> "https://vimeo.com/$videoId"
                    else -> null
                }
        }

        /**
         * 既知の variant に当てはまらないプロフィールアイテム。
         *
         * [rawJson] は転送・診断のために保持する未検証のネットワークデータである。呼び出し側は
         * パース・表示・内包する URL の利用の前に検証・サニタイズすること。
         */
        @Immutable
        data class Unknown(
            val id: String?,
            val type: String,
            val rawJson: String,
        ) : ProfileItem
    }

    /** プロフィールリンク。 */
    @Immutable
    data class ProfileLink(
        val url: String,
        val link: Platform,
    )

    /** プロフィールリンクの対応プラットフォーム。 */
    enum class Platform {
        BOOTH,
        FACEBOOK,
        FANZA,
        INSTAGRAM,
        LINE,
        PIXIV,
        TUMBLR,
        TWITTER,
        YOUTUBE,
        UNKNOWN,
    }
}
