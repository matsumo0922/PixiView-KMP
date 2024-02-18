@file:Suppress("UnstableApiUsage")

rootProject.name = "PixiView-KMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://www.jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://www.jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    versionCatalogs {
        create("libsMain") {
            from(files("gradle/libs.versions.toml"))
        }
        create("libsSubmodule") {
            from(files("precompose/gradle/libs.versions.toml"))
        }
    }
}

include(":composeApp")

include(":core:ui")
include(":core:billing")
include(":core:repository")
include(":core:datastore")
include(":core:model")
include(":core:common")

include(":feature:welcome")
include(":feature:library")
include(":feature:setting")
include(":feature:about")
include(":feature:post")
include(":feature:creator")

include(":precompose:precompose")
include(":precompose:precompose-koin")
include(":precompose:precompose-molecule")
include(":precompose:precompose-viewmodel")
