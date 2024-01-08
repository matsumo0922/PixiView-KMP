plugins {
    id("pixiview.kmp")
    id("pixiview.kmp.android.library")
    id("pixiview.kmp.android")
    id("pixiview.kmp.ios")
    id("pixiview.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.datastore"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.proto)
            api(libs.androidx.datastore.preferences)
        }
    }
}
