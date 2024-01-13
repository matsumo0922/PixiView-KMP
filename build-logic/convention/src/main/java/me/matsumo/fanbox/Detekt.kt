package me.matsumo.fanbox

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt(extension: DetektExtension) {
    extension.apply {
        toolVersion = libs.findVersion("detekt").get().toString()
        // 並列処理
        parallel = true
        // Detektの設定ファイル
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        // baseline 設定ファイル
        baseline = file("${project.rootDir}/config/detekt/baseline.xml")
        // デフォルト設定の上に自分の設定ファイルを適用する
        buildUponDefaultConfig = true
        // ルール違反があった場合にfailさせない
        ignoreFailures = false
        // ルール違反の自動修正を試みる
        autoCorrect = false
    }

    val reportMerge = if (!rootProject.tasks.names.contains("reportMerge")) {
        rootProject.tasks.register("reportMerge", ReportMergeTask::class) {
            output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
        }
    } else {
        rootProject.tasks.named("reportMerge") as TaskProvider<ReportMergeTask>
    }

    plugins.withType<io.gitlab.arturbosch.detekt.DetektPlugin> {
        tasks.withType<io.gitlab.arturbosch.detekt.Detekt> detekt@{
            finalizedBy(reportMerge)

            reportMerge.configure {
                input.from(this@detekt.xmlReportFile) // or .sarifReportFile
            }

            exclude {
                it.file.relativeTo(projectDir).startsWith("build")
            }
        }
    }
}
