plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.repository"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:datastore"))

            implementation(libsMain.bundles.ktor)
            implementation(libsMain.ksoup)
            implementation(libsMain.webview.compose)

            api(libsMain.kmp.paging.common)
        }

        androidMain.dependencies {
            api(libsMain.ktor.okhttp)
        }

        iosMain.dependencies {
            api(libsMain.ktor.darwin)
        }
    }
}
