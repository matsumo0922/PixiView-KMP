plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.secret.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.gms.services)
    implementation(libs.gms.oss)
}

gradlePlugin {
    plugins {
        register("KmpPlugin") {
            id = "pixiview.kmp"
            implementationClass = "KmpPlugin"
        }
        register("KmpAndroidPlugin") {
            id = "pixiview.kmp.android"
            implementationClass = "KmpAndroidPlugin"
        }
        register("KmpAndroidApplication") {
            id = "pixiview.kmp.android.application"
            implementationClass = "KmpAndroidApplication"
        }
        register("KmpAndroidLibrary") {
            id = "pixiview.kmp.android.library"
            implementationClass = "KmpAndroidLibrary"
        }
        register("KmpIosPlugin") {
            id = "pixiview.kmp.ios"
            implementationClass = "KmpIosPlugin"
        }
        register("DetektPlugin") {
            id = "pixiview.detekt"
            implementationClass = "DetektPlugin"
        }
    }
}
