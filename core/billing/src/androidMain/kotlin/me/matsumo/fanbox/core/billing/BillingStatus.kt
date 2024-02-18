package me.matsumo.fanbox.core.billing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.usecase.VerifyPlusUseCase
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.UserDataRepository

class BillingStatusImpl(
    private val userDataRepository: UserDataRepository,
    private val billingClient: BillingClient,
    private val verifyPlusUseCase: VerifyPlusUseCase,
    private val ioDispatcher: CoroutineDispatcher,
): BillingStatus {

    private val scope = CoroutineScope(ioDispatcher)

    override fun init() {
        billingClient.initialize()
    }

    override fun update() {
        scope.launch {
            suspendRunCatching { verifyPlusUseCase.invoke()!! }.isSuccess.also {
                userDataRepository.setPlusMode(it)
            }
        }
    }

    override fun finish() {
        // do nothing
    }
}
