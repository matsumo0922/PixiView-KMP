import androidx.compose.ui.window.ComposeUIViewController
import core.helper.appCacheDir
import core.helper.appFileDir
import io.github.xxfast.kstore.file.utils.CachesDirectory
import io.github.xxfast.kstore.file.utils.DocumentDirectory
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import platform.Foundation.NSFileManager
import platform.Foundation.NSStringFromClass
import platform.UIKit.UIApplicationMain

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun main() {
    val args = emptyArray<String>()

    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()

        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(AppDelegate))
        }
    }
}

@OptIn(ExperimentalKStoreApi::class)
fun MainViewController() = ComposeUIViewController {
    appFileDir = NSFileManager.defaultManager.DocumentDirectory?.relativePath
    appCacheDir = NSFileManager.defaultManager.CachesDirectory?.relativePath

    App()
}
