package me.matsumo.fanbox.core.billing

import io.github.aakira.napier.Napier
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.darwin.NSObject

class BillingInitializeImpl: BillingInitialize {

    private val transactionObserver = object : NSObject(), SKPaymentTransactionObserverProtocol {
        override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
            for (transaction in updatedTransactions) {
                when ((transaction as? SKPaymentTransaction)?.transactionState) {
                    SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                        Napier.d("payment transaction completed")
                        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                    }
                    SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                        Napier.d("payment transaction restored")
                        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                    }
                    SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                        Napier.d("payment transaction failed")
                        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    override fun init() {
        SKPaymentQueue.defaultQueue().addTransactionObserver(transactionObserver)
    }

    override fun finish() {
        SKPaymentQueue.defaultQueue().removeTransactionObserver(transactionObserver)
    }
}
