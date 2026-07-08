plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.matsumo.fanbox.core.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "me.matsumo.fanbox.core.resources"
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.components.resources)
        }
    }
}
