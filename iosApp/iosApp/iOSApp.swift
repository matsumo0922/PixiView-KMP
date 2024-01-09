import UIKit
import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
            GeometryReader { geo in
                ComposeViewController(
                    topSafeArea: Float(geo.safeAreaInsets.top),
                    bottomSafeArea: Float(geo.safeAreaInsets.bottom)
                )
                .edgesIgnoringSafeArea(.all)
                .onTapGesture {
                    UIApplication.shared.sendAction(
                        #selector(UIResponder.resignFirstResponder),
                        to: nil, from: nil, for: nil
                    )
                }
            }
		}
    }
}
