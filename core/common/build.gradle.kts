plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.common"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project.dependencies.platform(libsMain.koin.bom))

            api(libsMain.bundles.infra.api)
            api(libsMain.bundles.koin)
        }

        androidMain.dependencies {
            api(libsMain.koin.android)
        }
    }
}
