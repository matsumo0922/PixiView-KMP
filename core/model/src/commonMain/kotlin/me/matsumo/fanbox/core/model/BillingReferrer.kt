package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable

/** Plus 課金画面を開いた入口。課金画面のログの referrer には [logValue] を送る。 */
@Serializable
enum class BillingReferrer(val logValue: String) {
    /** ログイン完了直後の自動表示。 */
    ONBOARDING("onboarding"),

    /** ドロワーの Plus 購入項目。 */
    DRAWER("drawer"),

    /** About 画面の寄付項目。 */
    ABOUT_DONATE("about_donate"),

    /** テーマ設定の Dynamic Color の切り替え。 */
    DYNAMIC_COLOR("dynamic_color"),

    /** 設定の投稿詳細の無限スクロール。 */
    SETTING_INFINITY_POST_DETAIL("setting_infinity_post_detail"),

    /** 設定のグリッド表示。 */
    SETTING_GRID_MODE("setting_grid_mode"),

    /** 設定の制限付き投稿の非表示。 */
    SETTING_HIDE_RESTRICTED("setting_hide_restricted"),

    /** 設定のアプリロック。 */
    SETTING_APP_LOCK("setting_app_lock"),

    /** 設定の画像の自動プレビュー。 */
    SETTING_AUTO_IMAGE_PREVIEW("setting_auto_image_preview"),

    /** 設定の翻訳言語。 */
    SETTING_TRANSLATION_LANGUAGE("setting_translation_language"),

    /** 投稿詳細の本文の翻訳。 */
    POST_TRANSLATE("post_translate"),

    /** 投稿詳細のコメントの翻訳。 */
    COMMENT_TRANSLATE("comment_translate"),

    /** ライブラリの見つける画面のクリエイター内検索。 */
    DISCOVERY_CREATOR_SEARCH("discovery_creator_search"),

    /** クリエイター画面の一括ダウンロード。 */
    CREATOR_BULK_DOWNLOAD("creator_bulk_download"),

    /** クリエイター画面のクリエイター内検索。 */
    CREATOR_SEARCH("creator_search"),

    /** クリエイター画面の翻訳。 */
    CREATOR_TRANSLATE("creator_translate"),

    /** 解約予約ユーザー向けリテンション画面の年額プラン。 */
    BILLING_RETENTION("billing_retention"),
}
