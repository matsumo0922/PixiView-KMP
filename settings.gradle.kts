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
}

include(":composeApp")

include(":core:ui")
include(":core:billing")
include(":core:repository")
include(":core:datastore")
include(":core:model")
include(":core:logs")
include(":core:common")

include(":feature:welcome")
include(":feature:library")
include(":feature:setting")
include(":feature:about")
include(":feature:post")
include(":feature:creator")
