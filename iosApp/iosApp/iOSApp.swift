import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(PixiViewAppDelegate.self)
    var appDelegate
    
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

class PixiViewAppDelegate: NSObject, UIApplicationDelegate, ObservableObject {
    
    func application(application: UIApplication, launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        return true
    }
}

