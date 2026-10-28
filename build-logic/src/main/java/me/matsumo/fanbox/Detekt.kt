package me.matsumo.fanbox

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@Suppress("UNCHECKED_CAST")
internal fun Project.configureDetekt() {
    extensions.getByType<DetektExtension>().apply {
        toolVersion.set(libs.version("detekt"))
        // 並列処理
        parallel.set(true)
        // Detektの設定ファイル
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        // baseline 設定ファイル
        baseline.set(file("${project.rootDir}/config/detekt/baseline.xml"))
        // デフォルト設定の上に自分の設定ファイルを適用する
        buildUponDefaultConfig.set(true)
        // ルール違反があった場合に失敗させる
        ignoreFailures.set(false)
        // ルール違反の自動修正を試みる
        autoCorrect.set(false)
    }

    // 各モジュールでの Detekt 実行結果を一つにまとめるタスク（CIでコメントする際に使用）
    // FYI: https://detekt.dev/docs/introduction/reporting/#kotlin-dsl
    val reportMerge = if (!rootProject.tasks.names.contains("reportMerge")) {
        rootProject.tasks.register("reportMerge", ReportMergeTask::class) {
            output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
        }
    } else {
        rootProject.tasks.named("reportMerge") as TaskProvider<ReportMergeTask>
    }

    val reportFile = layout.buildDirectory.file("reports/detekt/detekt.xml")
    tasks.named("detekt", Detekt::class).configure {
        finalizedBy(reportMerge)
        reports.checkstyle.outputLocation.set(reportFile)

        setSource(
            project.fileTree(projectDir) {
                include("**/*.kt", "**/*.kts")
                exclude("**/resources/**", "**/build/**")
            },
        )
    }

    reportMerge.configure {
        // 通常の解析結果だけを集約し、型解決タスクや以前のレポートを混ぜない。
        input.from(reportFile)
    }
}
