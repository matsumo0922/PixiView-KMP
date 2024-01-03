package me.matsumo.fanbox

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import core.helper.appCacheDir
import core.helper.appFileDir

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appFileDir = filesDir?.path
        appCacheDir = cacheDir?.path

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
