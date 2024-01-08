plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android")
    id("pixiview.kmp.ios")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))

            implementation(libs.ktor.core)
            implementation(libs.moko.resources)
        }
    }
}
