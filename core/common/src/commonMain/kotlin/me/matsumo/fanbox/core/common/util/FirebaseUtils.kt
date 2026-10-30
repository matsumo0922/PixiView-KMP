package me.matsumo.fanbox.core.common.util

expect fun recordException(exception: Throwable)

/** クラッシュレポートに添える値を設定する。以降に記録されるクラッシュと non-fatal に [key] の [value] が付く。 */
expect fun setCrashReportKey(key: String, value: String)
