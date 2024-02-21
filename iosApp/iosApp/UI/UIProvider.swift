//
//  UIProvider.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/22.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

@objc public class UIProvider: NSObject {
    
    @objc public class func provideNativeAds() -> UIViewController {
        return UIHostingController(rootView: NativeAdView())
    }
}
