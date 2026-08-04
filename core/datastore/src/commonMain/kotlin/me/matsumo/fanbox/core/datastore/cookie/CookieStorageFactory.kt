package me.matsumo.fanbox.core.datastore.cookie

import eu.anifantakis.lib.ksafe.KSafe
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.logs.category.SettingsLog
import me.matsumo.fanbox.core.logs.logger.send

/**
 * 移行を挟んだ Cookie 保存先を組み立てる。
 *
 * [kSafe] と、旧 Room を開く [legacyStorageFactory] を受け取り、[MigratingFanboxCookieStorage] を返す。
 *
 * [kSafe] はアプリの生存期間中 1 つだけを使い、閉じないこと。`KSafe.close()` は同一プロセス内で
 * 作り直す場合のための操作であり、閉じた後はその実体への読み書きがすべて失敗する。
 * 同じファイルに対して複数の実体を作らないこと。
 */
internal fun createMigratingCookieStorage(
    kSafe: KSafe,
    legacyStorageFactory: () -> LegacyCookieStorage?,
): MigratingFanboxCookieStorage {
    val blobStore = KSafeCookieBlobStore(kSafe)

    return MigratingFanboxCookieStorage(
        secureStorage = SecureFanboxCookieStorage(blobStore),
        blobStore = blobStore,
        legacyStorageFactory = legacyStorageFactory,
        onMigrationEvent = ::reportMigrationEvent,
    )
}

/**
 * 移行の経過を記録する。
 *
 * Cookie の値、payload、資格情報から導いた識別子は記録しない。件数と段階だけを残す。
 */
private fun reportMigrationEvent(event: CookieMigrationEvent) {
    val description = when (event) {
        is CookieMigrationEvent.Started -> "started (${event.recordCount} records)"
        is CookieMigrationEvent.Succeeded -> "succeeded (${event.recordCount} records)"
        is CookieMigrationEvent.FallbackUsed -> "fallback at ${event.stage.name}"
        CookieMigrationEvent.CleanupPending -> "cleanup pending"
        CookieMigrationEvent.LogoutResumed -> "logout resumed"
    }

    Napier.i { "Cookie migration: $description" }

    SettingsLog.update(
        propertyName = "cookie_migration",
        oldValue = "",
        newValue = description,
    ).send()
}
