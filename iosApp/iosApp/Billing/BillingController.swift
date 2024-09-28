//
//  BillingController.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/17.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

@objc public class BillingController: NSObject {

    @objc public class func queryProducts() async -> [PlusProduct] {
        let products = await BillingClient().queryProducts().compactMap { $0 }
        
        return products.map { product in
            let style = product.priceFormatStyle
            let price = product.price as NSDecimalNumber
            
            let priceDouble = price.doubleValue
            let formattedPrice = style.format(product.price)
            
            return PlusProduct(price: priceDouble, formattedPrice: formattedPrice)
        }
    }

    @objc public class func purchase(id: String, onResult: @escaping (Int) -> Void) async {
        await BillingClient().requestPurchase(id: id, onResult: onResult)
    }

    @objc public class func refresh(onResult: @escaping (Bool) -> Void) async {
        await BillingClient().refreshStatus(onResult: onResult)
    }

    @objc public class func observeTransactionStatus(onResult: @escaping (Bool) -> Void) {
        BillingClient().observeTransactionUpdates(onResult: onResult)
    }
}

@objc public class PlusProduct: NSObject {

    @objc public var price: Double
    @objc public var formattedPrice: String

    @objc public init(price: Double, formattedPrice: String) {
        self.price = price
        self.formattedPrice = formattedPrice
    }
}
