import UIKit
import SwiftUI
import ComposeApp

struct ComposeViewController: UIViewControllerRepresentable {
    private let topSafeArea: Float
    private let bottomSafeArea: Float

    init(topSafeArea: Float, bottomSafeArea: Float) {
        self.topSafeArea = topSafeArea
        self.bottomSafeArea = bottomSafeArea
    }

    func makeUIViewController(context: Context) -> UIViewController {
        return ApplicationKt.MainViewController(
            topSafeArea: topSafeArea,
            bottomSafeArea: bottomSafeArea
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
