plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.compose")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

kotlin {
    android {
        namespace = "me.matsumo.fanbox.feature.creator"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kmp.backhandler)
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:repository"))
            implementation(project(":core:datastore"))
            implementation(project(":core:ui"))
            implementation(project(":core:logs"))
            implementation(project(":core:resources"))
        }
    }
}
