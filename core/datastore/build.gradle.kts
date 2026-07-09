@file:Suppress("UnusedPrivateProperty")

plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

kotlin {
    android {
        namespace = "me.matsumo.fanbox.core.datastore"

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:logs"))
            implementation(project(":core:resources"))

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.proto)
            api(libs.androidx.datastore.preferences)
        }

        val androidHostTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}
