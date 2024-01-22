package me.matsumo.fanbox.core.billing

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.repository.UserDataRepository
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.darwin.NSObject

class BillingInitializeImpl(
    private val userDataRepository: UserDataRepository,
    private val ioDispatcher: CoroutineDispatcher,
): BillingInitialize {

    private val scope = CoroutineScope(ioDispatcher)

    private val transactionObserver = object : NSObject(), SKPaymentTransactionObserverProtocol {
        override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
            for (transaction in updatedTransactions.filterIsInstance<SKPaymentTransaction>()) {
                when (transaction.transactionState) {
                    SKPaymentTransactionState.SKPaymentTransactionStateDeferred, SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                        scope.launch {
                            userDataRepository.setPlusMode(true)
                        }

                        Napier.d("purchase success: ${transaction.payment.productIdentifier}")
                        queue.finishTransaction(transaction)
                    }
                    SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                        scope.launch {
                            userDataRepository.setPlusMode(false)
                        }

                        Napier.d("purchase failed: ${transaction.payment.productIdentifier}")
                        queue.finishTransaction(transaction)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    override fun init() {
        Napier.d("billing client init")
        SKPaymentQueue.defaultQueue().addTransactionObserver(transactionObserver)
    }

    override fun finish() {
        Napier.d("billing client finish")
        SKPaymentQueue.defaultQueue().removeTransactionObserver(transactionObserver)
    }
}
