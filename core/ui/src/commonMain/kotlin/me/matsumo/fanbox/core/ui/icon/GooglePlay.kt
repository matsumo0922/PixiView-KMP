package me.matsumo.fanbox.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Outlined.GooglePlay: ImageVector
    get() = cache ?: materialIcon(name = "Filled.GooglePlay") {
        materialPath {
            moveTo(1.571f, 23.664f)
            lineToRelative(10.531f, -10.501f)
            lineToRelative(3.712f, 3.701f)
            lineToRelative(-12.519f, 6.941f)
            curveToRelative(-0.476f, 0.264f, -1.059f, 0.26f, -1.532f, -0.011f)
            lineToRelative(-0.192f, -0.13f)
            close()
            moveToRelative(9.469f, -11.56f)
            lineToRelative(-10.04f, 10.011f)
            verticalLineToRelative(-20.022f)
            lineToRelative(10.04f, 10.011f)
            close()
            moveToRelative(6.274f, -4.137f)
            lineToRelative(4.905f, 2.719f)
            curveToRelative(0.482f, 0.268f, 0.781f, 0.77f, 0.781f, 1.314f)
            reflectiveCurveToRelative(-0.299f, 1.046f, -0.781f, 1.314f)
            lineToRelative(-5.039f, 2.793f)
            lineToRelative(-4.015f, -4.003f)
            lineToRelative(4.149f, -4.137f)
            close()
            moveToRelative(-15.854f, -7.534f)
            curveToRelative(0.09f, -0.087f, 0.191f, -0.163f, 0.303f, -0.227f)
            curveToRelative(0.473f, -0.271f, 1.056f, -0.275f, 1.532f, -0.011f)
            lineToRelative(12.653f, 7.015f)
            lineToRelative(-3.846f, 3.835f)
            lineToRelative(-10.642f, -10.612f)
            close()
        }
    }.also {
        cache = it
    }

private var cache: ImageVector? = null
