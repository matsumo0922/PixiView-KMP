import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow

@OptIn(BetaInteropApi::class)
class AppDelegate @OverrideInit constructor() : UIResponder(), UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    private var _window: UIWindow? = null

    override fun window() = _window

    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?
    ): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds).apply {
            rootViewController = MainViewController()
            makeKeyAndVisible()
        }

        return true
    }
}
