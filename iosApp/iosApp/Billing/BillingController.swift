//
//  BillingController.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/17.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

@objc public class BillingController: NSObject {
    
    @objc public class func queryProduct() async -> PlusProduct? {
        guard let product = await BillingClient().queryProduct() else {
            return nil
        }
        
        let style = product.priceFormatStyle
        
        return PlusProduct(price: product.price, formattedPrice: style.format(product.price))
    }
    
    @objc public class func purchase(onResult: @escaping (Int) -> Void) async {
        await BillingClient().requestPurchase(onResult: onResult)
    }
    
    @objc public class func refresh(onResult: @escaping (Bool) -> Void) async {
        await BillingClient().refreshStatus(onResult: onResult)
    }
    
    @objc public class func observeTransactionStatus(onResult: @escaping (Bool) -> Void) {
        BillingClient().observeTransactionUpdates(onResult: onResult)
    }
}

@objc public class PlusProduct: NSObject {
    
    @objc public var price: Decimal
    @objc public var formattedPrice: String
    
    init(price: Decimal, formattedPrice: String) {
        self.price = price
        self.formattedPrice = formattedPrice
    }
}
