import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        InitHelperKt.doInitKoin()
        InitHelperKt.doInitNapier()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
