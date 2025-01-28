package me.matsumo.fanbox.core.common.util

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

actual fun recordException(exception: Throwable) {
    Firebase.crashlytics.recordException(exception)
}
