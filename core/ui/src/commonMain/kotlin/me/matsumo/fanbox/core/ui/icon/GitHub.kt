package me.matsumo.fanbox.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Outlined.GitHub: ImageVector
    get() = cache ?: materialIcon(name = "Filled.Github") {
        materialPath {
            moveTo(12f, 0f)
            curveToRelative(-6.626f, 0f, -12f, 5.373f, -12f, 12f)
            curveToRelative(0f, 5.302f, 3.438f, 9.8f, 8.207f, 11.387f)
            curveToRelative(0.599f, 0.111f, 0.793f, -0.261f, 0.793f, -0.577f)
            verticalLineToRelative(-2.234f)
            curveToRelative(-3.338f, 0.726f, -4.033f, -1.416f, -4.033f, -1.416f)
            curveToRelative(-0.546f, -1.387f, -1.333f, -1.756f, -1.333f, -1.756f)
            curveToRelative(-1.089f, -0.745f, 0.083f, -0.729f, 0.083f, -0.729f)
            curveToRelative(1.205f, 0.084f, 1.839f, 1.237f, 1.839f, 1.237f)
            curveToRelative(1.07f, 1.834f, 2.807f, 1.304f, 3.492f, 0.997f)
            curveToRelative(0.107f, -0.775f, 0.418f, -1.305f, 0.762f, -1.604f)
            curveToRelative(-2.665f, -0.305f, -5.467f, -1.334f, -5.467f, -5.931f)
            curveToRelative(0f, -1.311f, 0.469f, -2.381f, 1.236f, -3.221f)
            curveToRelative(-0.124f, -0.303f, -0.535f, -1.524f, 0.117f, -3.176f)
            curveToRelative(0f, 0f, 1.008f, -0.322f, 3.301f, 1.23f)
            curveToRelative(0.957f, -0.266f, 1.983f, -0.399f, 3.003f, -0.404f)
            curveToRelative(1.02f, 0.005f, 2.047f, 0.138f, 3.006f, 0.404f)
            curveToRelative(2.291f, -1.552f, 3.297f, -1.23f, 3.297f, -1.23f)
            curveToRelative(0.653f, 1.653f, 0.242f, 2.874f, 0.118f, 3.176f)
            curveToRelative(0.77f, 0.84f, 1.235f, 1.911f, 1.235f, 3.221f)
            curveToRelative(0f, 4.609f, -2.807f, 5.624f, -5.479f, 5.921f)
            curveToRelative(0.43f, 0.372f, 0.823f, 1.102f, 0.823f, 2.222f)
            verticalLineToRelative(3.293f)
            curveToRelative(0f, 0.319f, 0.192f, 0.694f, 0.801f, 0.576f)
            curveToRelative(4.765f, -1.589f, 8.199f, -6.086f, 8.199f, -11.386f)
            curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
            close()
        }
    }.also {
        cache = it
    }

private var cache: ImageVector? = null
