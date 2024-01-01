plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.kotlin.lite)
}
