package me.matsumo.fanbox.core.repository.utils

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.encodeURLParameter
import io.ktor.util.toMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.matsumo.fanbox.core.common.util.injectKoinInstance
import me.matsumo.fanbox.core.datastore.DummyDataStore
import me.matsumo.fanbox.core.repository.di.json
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
suspend inline fun <reified T> HttpResponse.parse(
    allowRange: IntRange = 200..299,
    f: ((T?) -> (Unit)) = {},
): T? {
    val requestUrl = request.url
    val isOK = this.status.value in allowRange

    val dummyDataStore = injectKoinInstance<DummyDataStore>()

    val dir = request.url.pathSegments.last()
    val params = request.url.parameters.toMap().toString()
    val dummyKey = "${dir}_${params.encodeURLParameter()}"
        .replace("%", "")
        .replace(".", "_")
        .replace("-", "_")
        .lowercase()

    val dummyData = dummyDataStore.getDummyData(dummyKey)

    if (isOK) {
        Napier.d("[SUCCESS] Ktor Request: $requestUrl")
    } else {
        Napier.d("[FAILED] Ktor Request: $requestUrl")
        Napier.d("[RESPONSE] ${this.bodyAsText().replace("\n", "")}")
    }

    if (dummyData != null) {
        Napier.d("[DUMMY] $dummyKey")
        return json.decodeFromString(serializer(typeOf<T>()) as KSerializer<T>, dummyData)
    }

    return (if (isOK) this.body<T>() else null).also {
        dummyDataStore.setDummyData(dummyKey, bodyAsText())
        f.invoke(it)
    }
}

fun HttpResponse.isSuccess(allowRange: IntRange = 200..299): Boolean {
    return (this.status.value in allowRange)
}

suspend fun HttpResponse.requireSuccess(allowRange: IntRange = 200..299): HttpResponse {
    if (!isSuccess(allowRange)) error("Request failed: ${this.status.value}, ${this.bodyAsText()}")
    return this
}
