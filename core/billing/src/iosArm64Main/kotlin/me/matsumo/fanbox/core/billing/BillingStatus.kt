package me.matsumo.fanbox.core.billing

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.swift.BillingController
import me.matsumo.fanbox.core.repository.SettingRepository

@OptIn(ExperimentalForeignApi::class)
class BillingStatusImpl(
    private val settingRepository: SettingRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : BillingStatus {

    private val scope = CoroutineScope(ioDispatcher)

    override fun init() {
        BillingController.observeTransactionStatusOnResult {
            scope.launch {
                settingRepository.setPlusMode(it)
            }
        }
    }

    override fun update() {
        scope.launch {
            BillingController.refreshOnResult(
                onResult = {
                    scope.launch {
                        Napier.d { "refresh billing status: $it" }
                        settingRepository.setPlusMode(it)
                    }
                },
                completionHandler = {
                    cancel()
                },
            )
        }
    }

    override fun finish() {
        // do nothing
    }
}
