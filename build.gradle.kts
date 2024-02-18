plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libsMain.plugins.android.application) apply false
    alias(libsMain.plugins.android.library) apply false
    alias(libsMain.plugins.kmp) apply false
    alias(libsMain.plugins.kmpCompose) apply false
    alias(libsMain.plugins.kmpComplete) apply false
    alias(libsMain.plugins.kmpSwiftKlib) apply false
    alias(libsMain.plugins.kotlin.android) apply false
    alias(libsMain.plugins.kotlin.serialization) apply false
    alias(libsMain.plugins.firebase.crashlytics) apply false
    alias(libsMain.plugins.libraries) apply false
    alias(libsMain.plugins.detekt) apply false
    alias(libsMain.plugins.ksp) apply false
    alias(libsMain.plugins.gms) apply false
}

extra.apply {
    set("precomposeVersion", "1.5.10")

    set("jvmTarget", "11")

    // Android configurations
    set("android-compile", 34)
    set("android-build-tools", "34.0.0")
    set("androidMinSdk", 21)
    set("androidTargetSdk", 34)

    // Js & Node
    set("webpackCliVersion", "5.1.4")
    set("nodeVersion", "16.13.0")

    set("ktlintVersion", "0.50.0")
}
