plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.kmp.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

android {
    namespace = "me.matsumo.fanbox.core.billing"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:common"))
            implementation(project(":core:repository"))
            implementation(project(":core:datastore"))
            implementation(project(":core:logs"))
            implementation(project(":core:resources"))

            implementation(libs.bundles.purchase)
        }
    }
}
