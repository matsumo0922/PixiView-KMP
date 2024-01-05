package me.matsumo.fanbox.core.model.fanbox

import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import io.ktor.http.Url

data class FanboxCreatorDetail(
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
    val user: FanboxUser,
) {
    val supportingBrowserUrl get() = "https://www.fanbox.cc/creators/supporting/@${user.creatorId}"

    data class ProfileItem(
        val id: String,
        val imageUrl: String?,
        val thumbnailUrl: String?,
        val type: String,
    )

    data class ProfileLink(
        val url: String,
        val link: Platform,
    )

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
        ;

        companion object {
            fun fromUrl(url: String): Platform {
                val hostname = Url(url).host.lowercase()

                return when {
                    "booth.pm" in hostname -> BOOTH
                    "facebook.com" in hostname -> FACEBOOK
                    "dmm.co.jp" in hostname -> FANZA
                    "instagram.com" in hostname -> INSTAGRAM
                    "line.me" in hostname -> LINE
                    listOf("www.pixiv.net", "touch.pixiv.net", "pixiv.me", "fanbox.cc").any { it in hostname } -> PIXIV
                    "tumblr.com" in hostname -> TUMBLR
                    "twitter.com" in hostname -> TWITTER
                    listOf("youtu.be", "youtube.com").any { it in hostname } -> YOUTUBE
                    else -> UNKNOWN
                }
            }
        }
    }

    companion object {
        fun dummy() = FanboxCreatorDetail(
            creatorId = CreatorId("island"),
            coverImageUrl = null,
            description = "子供の頃、藤子不二雄・赤塚不二夫氏の影響を受け、中高生の頃は吾妻ひでお氏、現在は秋月りす氏の影響を受け、なにがなにやらわからない系の絵になってます。簡単に言うと「昭和の絵」。（苦笑）\n\n活動記録\n\n1996年頃から16年近く業界紙にOL4コマ、経済金融4コマ、歴史4コマ漫画を連載して最近は創作オリジナルストーリー漫画に注力しております。\n\n2014年3月　「いい加減でいこう」という個人サークルで同人活動を始めました。\n\n 2016年、「青空高く！」という女子小学生ゴルフマンガで某講談社のKFS講談社マンガ賞を頂きました。講談社漫画賞ではないので念のため。(笑)\n\n2019年3月に日本漫画家協会に入ったので取り敢えずクリエイター系を名乗ってます。取り敢えずです。\n\n2019年6月19日よりpixivFANBOXも始めました。\n\n・応援して下さる方々との交流を更に深める活動をします。　特別描き下ろしイラストとかマンガとか。\n・頂いた支援は創作活動にすべて使います。決して飲食代には使いません。（笑）\n\n\n",
            hasAdultContent = true,
            hasBoothShop = false,
            isAcceptingRequest = false,
            isFollowed = false,
            isStopped = false,
            isSupported = true,
            profileItems = emptyList(),
            profileLinks = listOf(
                ProfileLink(
                    url = "https://www.pixiv.net/fanbox/creator/24164271",
                    link = Platform.PIXIV,
                ),
                ProfileLink(
                    url = "https://twitter.com/island_ol",
                    link = Platform.TWITTER,
                ),
            ),
            user = FanboxUser.dummy(),
        )
    }
}
