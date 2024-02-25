package me.matsumo.fanbox.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector


val Icons.Outlined.Apple: ImageVector
    get() = cache ?: materialIcon(name = "Filled.Apple") {
        materialPath {
            moveTo(22f, 17.607f)
            curveToRelative(-0.786f, 2.28f, -3.139f, 6.317f, -5.563f, 6.361f)
            curveToRelative(-1.608f, 0.031f, -2.125f, -0.953f, -3.963f, -0.953f)
            curveToRelative(-1.837f, 0f, -2.412f, 0.923f, -3.932f, 0.983f)
            curveToRelative(-2.572f, 0.099f, -6.542f, -5.827f, -6.542f, -10.995f)
            curveToRelative(0f, -4.747f, 3.308f, -7.1f, 6.198f, -7.143f)
            curveToRelative(1.55f, -0.028f, 3.014f, 1.045f, 3.959f, 1.045f)
            curveToRelative(0.949f, 0f, 2.727f, -1.29f, 4.596f, -1.101f)
            curveToRelative(0.782f, 0.033f, 2.979f, 0.315f, 4.389f, 2.377f)
            curveToRelative(-3.741f, 2.442f, -3.158f, 7.549f, 0.858f, 9.426f)
            close()
            moveToRelative(-5.222f, -17.607f)
            curveToRelative(-2.826f, 0.114f, -5.132f, 3.079f, -4.81f, 5.531f)
            curveToRelative(2.612f, 0.203f, 5.118f, -2.725f, 4.81f, -5.531f)
            close()
        }
    }.also {
        cache = it
    }

private var cache: ImageVector? = null
