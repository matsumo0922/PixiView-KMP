plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.ui"
}

dependencies {
    api(libs.bundles.infra.api)
    implementation(libs.libraries.core)
}
