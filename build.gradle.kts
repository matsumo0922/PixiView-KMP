plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libsMain.plugins.android.application) apply false
    alias(libsMain.plugins.android.library) apply false
    alias(libsMain.plugins.kmp) apply false
    alias(libsMain.plugins.kmpCompose) apply false
    alias(libsMain.plugins.kmpComplete) apply false
    alias(libsMain.plugins.kmpSwiftKlib) apply false
    alias(libsMain.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libsMain.plugins.firebase.crashlytics) apply false
    alias(libsMain.plugins.libraries) apply false
    alias(libsMain.plugins.detekt) apply false
    alias(libsMain.plugins.ksp) apply false
    alias(libsMain.plugins.gms) apply false
}
