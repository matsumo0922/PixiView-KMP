plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    compileOnly(gradleKotlinDsl())
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.secret.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.build.konfig.gradlePlugin)
    implementation(libs.gms.services)
}

gradlePlugin {
    plugins {
        register("AndroidApplicationPlugin") {
            id = "pixiview.primitive.android.application"
            implementationClass = "primitive.AndroidApplicationPlugin"
        }
        register("AndroidLibraryPlugin") {
            id = "pixiview.primitive.android.library"
            implementationClass = "primitive.AndroidLibraryPlugin"
        }
        register("KmpPlugin") {
            id = "pixiview.primitive.kmp.common"
            implementationClass = "primitive.KmpCommonPlugin"
        }
        register("KmpAndroidPlugin") {
            id = "pixiview.primitive.kmp.android"
            implementationClass = "primitive.KmpAndroidPlugin"
        }
        register("KmpAndroidCompose") {
            id = "pixiview.primitive.kmp.compose"
            implementationClass = "primitive.KmpComposePlugin"
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
