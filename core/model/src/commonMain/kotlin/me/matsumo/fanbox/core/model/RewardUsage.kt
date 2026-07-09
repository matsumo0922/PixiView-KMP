package me.matsumo.fanbox.core.model

/** リワード広告で一時解放する機能の用途。 */
enum class RewardUsage(
    val storageKey: String,
    val dailyLimit: Int,
) {
    /** クリエイター投稿の一括ダウンロード。 */
    BulkDownload(
        storageKey = "bulk_download",
        dailyLimit = 1,
    ),

    /** クリエイター投稿検索。 */
    CreatorSearch(
        storageKey = "creator_search",
        dailyLimit = 1,
    ),

    /** クリエイター概要の翻訳。 */
    CreatorTranslation(
        storageKey = "creator_translation",
        dailyLimit = 1,
    ),
}
