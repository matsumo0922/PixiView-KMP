package me.matsumo.fanbox.core.common.util

import androidx.compose.runtime.snapshots.SnapshotStateList

fun <T> List<T>.diff(list: List<T>): List<T> {
    val diff1 = this - list.toSet()
    val diff2 = list - this.toSet()
    return diff1 + diff2
}

fun <T> SnapshotStateList<T>.removeIf(predicate: (T) -> Boolean) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (predicate(iterator.next())) {
            iterator.remove()
        }
    }
}
