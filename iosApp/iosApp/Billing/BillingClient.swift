//
//  BillingClient.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/17.
//  Copyright © 2024 orgName. All rights reserved.
//

import StoreKit

class BillingClient {
    
    func observeTransactionUpdates(onResult: @escaping (Bool) -> Void) {
        Task(priority: .background) {
            for await verificationResult in Transaction.updates {
                guard case .verified(let transcation) = verificationResult else { continue }

                if transcation.revocationDate != nil {
                    // 払い戻し済み
                    onResult(false)
                } else if let expirationDate = transcation.expirationDate, Date() < expirationDate && !transcation.isUpgraded {
                    // 有効
                    onResult(true)
                }
                
                await transcation.finish()
            }
        }
    }
    
    func queryProducts() async -> [Product?] {
        do {
            let productPlus = try await Product.products(for: ["plus"]).first
            let productPlusYear = try await Product.products(for: ["plus_year"]).first
            
            return [productPlus, productPlusYear]
        } catch {
            return []
        }
    }
    
    func refreshStatus(onResult: (Bool) -> Void) async {
        var validSubscription: Transaction?
        
        for await verificationResult in Transaction.currentEntitlements {
            if case .verified(let transaction) = verificationResult, transaction.productType == .autoRenewable && !transaction.isUpgraded {
                validSubscription = transaction
            }
        }
        
        onResult(validSubscription != nil)
    }
    
    func requestPurchase(id: String, onResult: @escaping (Int) -> Void) async {
        do {
            let product = await queryProducts().first { $0?.id == id }
            let transaction = try await purchase(product!!)
            
            onResult(0)
            
            await transaction?.finish()
        } catch {
            print("Failed requestPurchase: \(error)")
            onResult(translateErrorToInt(error: error as? SubscribeError))
        }
    }
    
    private func purchase(_ product: Product) async throws -> Transaction? {
        let result: Product.PurchaseResult
        
        do {
            result = try await product.purchase()
        } catch Product.PurchaseError.productUnavailable {
            throw SubscribeError.productUnavailable
        } catch Product.PurchaseError.purchaseNotAllowed {
            throw SubscribeError.purchaseNotAllowed
        } catch {
            throw SubscribeError.otherError
        }
        
        switch result {
        case .success(let verification):
            return try verifyTransaction(veridication: verification)
        case .userCancelled:
            throw SubscribeError.userCancelled
        case .pending:
            throw SubscribeError.pending
        @unknown default:
            throw SubscribeError.otherError
        }
    }
    
    private func verifyTransaction(veridication: VerificationResult<Transaction>) throws -> Transaction {
        switch veridication {
        case .verified(let transaction):
            return transaction
        case .unverified:
            throw SubscribeError.failedVerification
        }
    }
    
    private func translateErrorToInt(error: SubscribeError?) -> Int {
        switch error {
        case .userCancelled:
            return 1
        case .pending:
            return 2
        case .productUnavailable:
            return 3
        case .purchaseNotAllowed:
            return 4
        case .failedVerification:
            return 5
        default:
            return 6
        }
    }
}

private enum SubscribeError: LocalizedError {
    case userCancelled // ユーザーによって購入がキャンセルされた
    case pending // クレジットカードが未設定などの理由で購入が保留された
    case productUnavailable // 指定した商品が無効
    case purchaseNotAllowed // OSの支払い機能が無効化されている
    case failedVerification // トランザクションデータの署名が不正
    case otherError // その他のエラー
}
