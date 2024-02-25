plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

dependencies {
    implementation(libsMain.android.gradlePlugin)
    implementation(libsMain.kotlin.gradlePlugin)
    implementation(libsMain.secret.gradlePlugin)
    implementation(libsMain.detekt.gradlePlugin)
    implementation(libsMain.moko.resources.gradlePlugin)
    implementation(libsMain.build.konfig.gradlePlugin)
    implementation(libsMain.gms.services)
    implementation(libsMain.gms.oss)
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
        register("KmpResources") {
            id = "pixiview.primitive.kmp.resources"
            implementationClass = "primitive.KmpResources"
        }
        register("DetektPlugin") {
            id = "pixiview.primitive.detekt"
            implementationClass = "primitive.DetektPlugin"
        }
    }
}
