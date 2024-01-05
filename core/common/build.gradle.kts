plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android")
    id("pixiview.kmp.ios")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.common"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.bundles.infra.api)
                api(libs.bundles.ui.common.api)
            }
        }
    }
}
