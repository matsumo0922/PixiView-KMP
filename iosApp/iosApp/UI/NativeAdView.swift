//
//  NativeAdView.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/21.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import GoogleMobileAds
import ComposeApp

struct NativeAdView: View {
    
    @EnvironmentObject
    private var sceneDelegate: PixiViewSceneDelegate
    
    @StateObject
    private var model = NativeAdModel()
    
    var body: some View {
        VStack {
            Text("NativeAd")
//            if let nativeAd = model.nativeAd {
//                NativeAdItem(nativeAd: nativeAd)
//            }
        }.onAppear(perform: loadAd)
    }
    
    private func loadAd() {
        model.load(
            windowScene: sceneDelegate.scene,
            rootViewController: sceneDelegate.window?.rootViewController
        )
    }
}

private struct NativeAdItem: UIViewRepresentable {
    
    let nativeAd: GADNativeAd
    
    func makeUIView(context: Context) -> GADNativeAdView {
        let nativeAdView: GADNativeAdView = Bundle.main.loadNibNamed("NativeAdView", owner: nil, options: nil)?.first as! GADNativeAdView
        
        (nativeAdView.bodyView as? UILabel)?.text = nativeAd.body
        nativeAdView.bodyView?.isHidden = nativeAd.body == nil
        
        (nativeAdView.callToActionView as? UIButton)?.setTitle(nativeAd.callToAction, for: .normal)
        nativeAdView.callToActionView?.isHidden = nativeAd.callToAction == nil
        
        (nativeAdView.iconView as? UIImageView)?.image = nativeAd.icon?.image
        nativeAdView.iconView?.isHidden = nativeAd.icon == nil
        
        (nativeAdView.storeView as? UILabel)?.text = nativeAd.store
        nativeAdView.storeView?.isHidden = nativeAd.store == nil
        
        (nativeAdView.priceView as? UILabel)?.text = nativeAd.price
        nativeAdView.priceView?.isHidden = nativeAd.price == nil
        
        (nativeAdView.advertiserView as? UILabel)?.text = nativeAd.advertiser
        nativeAdView.advertiserView?.isHidden = nativeAd.advertiser == nil
        
        nativeAdView.callToActionView?.isUserInteractionEnabled = false
        
        nativeAdView.nativeAd = nativeAd
        
        return nativeAdView
    }
    
    func updateUIView(_ uiView: GADNativeAdView, context: Context) {
        // do nothing
    }
}

private class NativeAdModel: NSObject, ObservableObject, GADNativeAdLoaderDelegate {
    
    @Published 
    var nativeAd: GADNativeAd?
    
    private var adLoader: GADAdLoader?
    private var adUnitID: String
    
    override init() {
        self.adUnitID = KeyManager().getValue(key: "ADMOB_IOS_NATIVE_AD_UNIT_ID") as! String
    }
    
    func load(windowScene: UIWindowScene?, rootViewController: UIViewController?) {
        let adLoader = GADAdLoader(adUnitID: adUnitID, rootViewController: rootViewController, adTypes: [.native], options: nil)
        self.adLoader = adLoader
        adLoader.delegate = self
        
        let request = GADRequest()
        request.scene = windowScene
        adLoader.load(request)
    }
    
    func adLoader(_ adLoader: GADAdLoader, didReceive nativeAd: GADNativeAd) {
        self.nativeAd = nativeAd
    }
    
    func adLoader(_ adLoader: GADAdLoader, didFailToReceiveAdWithError error: Error) {
        // do nothing
    }
}
