package me.matsumo.fanbox.core.ui.ads

/** インタースティシャル広告の表示を試みた結果。[logValue] はログの result に送る値。 */
enum class InterstitialAdShowResult(val logValue: String) {
    /** 表示できた */
    Shown("shown"),

    /** 広告をロード中で、表示できなかった */
    Loading("loading"),

    /** 広告のロードに失敗し、再試行を待っているため表示できなかった */
    LoadFailed("load_failed"),

    /** ロード済みの広告が失効していたため、破棄して再ロードした */
    Expired("expired"),

    /** 広告の表示に失敗した */
    ShowFailed("show_failed"),

    /** 広告のロードが無効なため表示できなかった */
    Disabled("disabled"),
}
