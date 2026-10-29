package me.matsumo.fanbox

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt() {
    val extension = extensions.getByType<DetektExtension>()
    extension.toolVersion.set(libs.version("detekt"))
    // 並列処理
    extension.parallel.set(true)
    // Detektの設定ファイル
    extension.config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
    // baseline 設定ファイル
    extension.baseline.set(file("${project.rootDir}/config/detekt/baseline.xml"))
    // デフォルト設定の上に自分の設定ファイルを適用する
    extension.buildUponDefaultConfig.set(true)
    // ルール違反があった場合に失敗させる
    extension.ignoreFailures.set(false)
    // ルール違反の自動修正を試みる
    extension.autoCorrect.set(false)

    val generatedDirectory = layout.buildDirectory
    tasks.withType<Detekt>().configureEach {
        // source set の構成は維持し、Compose resources・KSP などの生成物だけを検査対象から外す。
        exclude { it.file.toPath().startsWith(generatedDirectory.get().asFile.toPath()) }
    }

    val fullDetektTasks = tasks.withType<Detekt>().matching {
        it.name in setOf("detektMainAndroid", "detektHostTestAndroid", "detektDebug", "detektDebugUnitTest", "detektDebugAndroidTest")
    }
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(fullDetektTasks)
    }

    // 各モジュールでの Detekt 実行結果を一つにまとめるタスク（CIでコメントする際に使用）
    // FYI: https://detekt.dev/docs/introduction/reporting/#kotlin-dsl
    val reportMerge = if (!rootProject.tasks.names.contains("reportMerge")) {
        rootProject.tasks.register("reportMerge", ReportMergeTask::class) {
            output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
        }
    } else {
        rootProject.tasks.named<ReportMergeTask>("reportMerge")
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
