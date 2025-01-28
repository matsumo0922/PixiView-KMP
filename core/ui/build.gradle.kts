@file:Suppress("UnusedPrivateProperty")

plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.ui"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:model"))
                implementation(project(":core:common"))
                implementation(project(":core:repository"))
                implementation(project(":core:datastore"))
                implementation(project(":core:logs"))
                implementation(project(":core:resources"))

                api(libs.bundles.ui.common.api)
                api(libs.bundles.cupertino)

                // Compose
                api(compose.runtime)
                api(compose.runtimeSaveable)
                api(compose.foundation)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.material)
                api(compose.material3)
                api(compose.ui)
                api(compose.materialIconsExtended)

                api(libs.adaptive)
                api(libs.adaptive.layout)

                api(libs.calf.ui)
                api(libs.placeholder)
                api(libs.rich.editor)
                api(libs.autolink)
                api(libs.play.service.ads)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.bundles.media3)
                api(libs.bundles.ui.android.api)
            }
        }
    }
}
