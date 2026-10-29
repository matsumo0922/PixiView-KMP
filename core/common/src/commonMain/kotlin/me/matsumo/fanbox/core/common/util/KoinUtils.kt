package me.matsumo.fanbox.core.common.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

inline fun <reified T> injectKoinInstance(): T = object : KoinComponent {
    val value: T by inject()
}.value
