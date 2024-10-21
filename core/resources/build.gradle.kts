plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.resources"
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.matsumo.fanbox.core.resources"
    generateResClass = always
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(compose.components.resources)
        }
    }
}
