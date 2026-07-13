plugins {
    id("pixiview.primitive.kmp.common")
    id("pixiview.primitive.android.library")
    id("pixiview.primitive.kmp.android")
    id("pixiview.primitive.kmp.ios")
    id("pixiview.primitive.detekt")
}

kotlin {
    android {
        namespace = "me.matsumo.fanbox.core.billing"

        withHostTest {}
    }

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

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        getByName("androidHostTest").dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}
