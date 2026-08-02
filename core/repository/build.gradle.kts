@file:Suppress("UnusedPrivateProperty")

plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

kotlin {
    android {
        namespace = "me.matsumo.fanbox.core.repository"

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:datastore"))
            implementation(project(":core:logs"))
            implementation(project(":core:resources"))

            implementation(libs.ksoup)
            implementation(libs.openai.client)
            implementation(libs.webview.compose)

            api(libs.kmp.paging.common)
            api(libs.fankt.fanbox.persistence.room)
        }

        // fankt 0.1.0 は Ktor を implementation で持つため、HTTP エンジンの提供は利用側の責務に
        // なる。ソース上の参照は無いが、外すと実行時にエンジンが見つからず通信できない。
        androidMain.dependencies {
            api(libs.ktor.okhttp)
        }

        iosMain.dependencies {
            api(libs.ktor.darwin)
        }

        val androidHostTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
