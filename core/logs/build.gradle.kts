plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.logs"
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
