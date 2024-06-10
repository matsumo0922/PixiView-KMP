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
        }

        androidMain.dependencies {
            api(libs.bundles.billing)
        }

        iosArm64()
    }

    if (System.getProperty("os.name").lowercase().contains("mac")) {
        listOf(iosArm64()).forEach {
            it.compilations {
                val main by getting {
                    cinterops {
                        create("BillingSwift")
                    }

                    swiftklib {
                        create("BillingSwift") {
                            minIos = 15

                            path = file("../../iosApp/iosApp/Billing")
                            packageName("me.matsumo.fanbox.core.billing.swift")
                        }
                    }
                }
            }
        }
    }
}
