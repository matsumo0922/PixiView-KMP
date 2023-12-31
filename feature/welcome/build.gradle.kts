plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android.compose")
    id("pixiview.kmp.android")
    id("pixiview.kmp.ios")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.feature.welcome"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:repository"))
            implementation(project(":core:datastore"))
            implementation(project(":core:ui"))

            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.webview.compose)
        }
    }
}
