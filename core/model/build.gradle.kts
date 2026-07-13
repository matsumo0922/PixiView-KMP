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
        namespace = "me.matsumo.fanbox.core.model"

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:resources"))

            implementation(compose.components.resources)
            implementation(libs.ktor.core)

            api(libs.fankt.fanbox)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        getByName("androidHostTest").dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}
