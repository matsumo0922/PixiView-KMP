//
//  BannerAdView.swift
//  iosApp
//
//  Created by daichi-matsumoto on 2024/02/23.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import GoogleMobileAds

struct BannerAdView: View {
    
    var body: some View {
        BannerAdItem()
    }
}

struct BannerAdItem: UIViewControllerRepresentable {
    
    @State
    private var viewWidth: CGFloat = .zero
    
    private let bannerView = GADBannerView()
    private var adUnitID = "ca-app-pub-3940256099942544/2934735716"  // Test用のID
    
    init() {
        self.adUnitID = KeyManager().getValue(key: "ADMOB_IOS_BANNER_AD_UNIT_ID") as! String
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        let bannerViewController = BannerViewController()
        bannerView.adUnitID = adUnitID
        bannerView.rootViewController = bannerViewController
        bannerViewController.view.addSubview(bannerView)
        
        // Tell the bannerViewController to update our Coordinator when the ad width changes.
        bannerViewController.delegate = context.coordinator
        
        return bannerViewController
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        guard viewWidth != .zero else { return }
        
        // Request a banner ad with the updated viewWidth.
        bannerView.adSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth)
        bannerView.load(GADRequest())
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, BannerViewControllerWidthDelegate {
        let parent: BannerAdItem

        init(_ parent: BannerAdItem) {
            self.parent = parent
        }

        // MARK: - BannerViewControllerWidthDelegate methods
        func bannerViewController(_ bannerViewController: BannerViewController, didUpdate width: CGFloat) {
            // Pass the viewWidth from Coordinator to BannerView.
            parent.viewWidth = width
        }
    }
}

protocol BannerViewControllerWidthDelegate: AnyObject {
    func bannerViewController(_ bannerViewController: BannerViewController, didUpdate width: CGFloat)
}

class BannerViewController: UIViewController {
    weak var delegate: BannerViewControllerWidthDelegate?
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        // Tell the delegate the initial ad width.
        delegate?.bannerViewController(self, didUpdate: view.frame.inset(by: view.safeAreaInsets).size.width)
    }
    
    override func viewWillTransition(
        to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator
    ) {
        coordinator.animate { _ in
            // do nothing
        } completion: { _ in
            // Notify the delegate of ad width changes.
            self.delegate?.bannerViewController(self, didUpdate: self.view.frame.inset(by: self.view.safeAreaInsets).size.width)
        }
    }
}
