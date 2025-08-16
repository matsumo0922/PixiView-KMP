plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.secret.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.build.konfig.gradlePlugin)
    implementation(libs.gms.services)
    implementation(libs.gms.oss)
}

gradlePlugin {
    plugins {
        register("KmpPlugin") {
            id = "pixiview.primitive.kmp.common"
            implementationClass = "primitive.KmpCommonPlugin"
        }
        register("KmpAndroidPlugin") {
            id = "pixiview.primitive.kmp.android"
            implementationClass = "primitive.KmpAndroidPlugin"
        }
        register("KmpAndroidApplication") {
            id = "pixiview.primitive.kmp.android.application"
            implementationClass = "primitive.KmpAndroidApplication"
        }
        register("KmpAndroidLibrary") {
            id = "pixiview.primitive.kmp.android.library"
            implementationClass = "primitive.KmpAndroidLibrary"
        }
        register("KmpAndroidCompose") {
            id = "pixiview.primitive.kmp.android.compose"
            implementationClass = "primitive.KmpAndroidCompose"
        }
        register("KmpIosPlugin") {
            id = "pixiview.primitive.kmp.ios"
            implementationClass = "primitive.KmpIosPlugin"
        }
        register("DetektPlugin") {
            id = "pixiview.primitive.detekt"
            implementationClass = "primitive.DetektPlugin"
        }
    }
}
