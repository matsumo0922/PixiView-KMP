import UIKit
import SwiftUI
import GoogleMobileAds
import ComposeApp

struct ComposeViewController: UIViewControllerRepresentable {
    private let topSafeArea: Float
    private let bottomSafeArea: Float

    init(topSafeArea: Float, bottomSafeArea: Float) {
        self.topSafeArea = topSafeArea
        self.bottomSafeArea = bottomSafeArea
    }

    func makeUIViewController(context: Context) -> UIViewController {
        initTools()

        return ApplicationKt.MainViewController(
            topSafeArea: topSafeArea,
            bottomSafeArea: bottomSafeArea,
            iosUis: makeUIs()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private func initTools() {
        InitHelperKt.doInitKoin()
        InitHelperKt.doInitNapier()
        InitHelperKt.doInitCoil()
    }

    private func makeUIs() -> [String: @convention(block)() -> UIViewController?] {
        return [
            "NativeAdView": { UIHostingController(rootView: NativeAdView()) },
            "BannerAdView": { UIHostingController(rootView: BannerAdView()) }
        ]
    }
}
