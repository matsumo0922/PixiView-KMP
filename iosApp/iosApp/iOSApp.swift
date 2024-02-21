import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(PixiViewDelegate.self)
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

class PixiViewDelegate: NSObject, UIApplicationDelegate, ObservableObject {
    
    func application(application: UIApplication, launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        return true
    }
}

class PixiViewSceneDelegate: NSObject, UIWindowSceneDelegate, ObservableObject {
    
    var scene: UIWindowScene?
    var window: UIWindow?
    
    func windowScene(_ windowScene: UIWindowScene, performActionFor shortcutItem: UIApplicationShortcutItem) async -> Bool {
        scene = windowScene
        window = windowScene.keyWindow
        
        return true
    }
}

extension PixiViewDelegate {
    
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        
        
        let configuration = UISceneConfiguration(name: nil,sessionRole: connectingSceneSession.role)
        
        if connectingSceneSession.role == .windowApplication {
            configuration.delegateClass = PixiViewSceneDelegate.self
        }
        
        return configuration
    }
}
