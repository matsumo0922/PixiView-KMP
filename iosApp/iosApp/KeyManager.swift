//
//  KeyManager.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/23.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

struct KeyManager {
    
    private let keyFilePath = Bundle.main.path(forResource: "secret", ofType: "plist")
    
    func getKeys() -> NSDictionary? {
        guard let keyFilePath = keyFilePath else { return nil }
        return NSDictionary(contentsOfFile: keyFilePath)
    }
    
    func getValue(key: String) -> AnyObject? {
        guard let keys = getKeys() else { return nil }
        return keys[key]! as AnyObject
    }
}
