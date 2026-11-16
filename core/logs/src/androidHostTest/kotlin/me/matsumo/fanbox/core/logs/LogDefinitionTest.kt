package me.matsumo.fanbox.core.logs

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

/**
 * ログ定義（`scripts/daifuku/log_definitions`）から作られる、Firebase Analytics のイベント名を検証するテスト。
 *
 * Firebase Analytics に送る名前は「カテゴリ_イベント名」で、40 文字を超える名前のイベントは送信の時点で破棄される。
 * カテゴリはログ定義のファイル名、イベント名は `## ` で始まる見出しから作られる。
 */
class LogDefinitionTest {

    // テストはモジュールのディレクトリ（core/logs）で実行されるため、リポジトリの根から辿る。
    private val definitionDirectory = File("../../scripts/daifuku/log_definitions")

    @Test
    fun everyEventNameFitsTheFirebaseAnalyticsLimit() {
        val eventNames = definitionEventNames()
        val tooLongNames = eventNames.filter { it.length > 40 }

        assertTrue(eventNames.isNotEmpty(), "No event definitions were found in ${definitionDirectory.absolutePath}")
        assertTrue(tooLongNames.isEmpty(), "Event names longer than 40 characters: ${tooLongNames.joinToString { "$it (${it.length})" }}")
    }

    private fun definitionEventNames(): List<String> = definitionDirectory
        .listFiles { file -> file.extension == "md" }
        .orEmpty()
        .flatMap { file -> eventNamesOf(file) }

    private fun eventNamesOf(file: File): List<String> = file
        .readLines()
        .filter { it.startsWith("## ") }
        .map { heading -> "${file.nameWithoutExtension}_${heading.removePrefix("## ").trim()}" }
}
