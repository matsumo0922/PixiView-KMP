package me.matsumo.fanbox.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Outlined.Twitter: ImageVector
    get() = cache ?: materialIcon(name = "Filled.Twitter") {
        materialPath {
            moveTo(24f, 4.557f)
            curveToRelative(-0.883f, 0.392f, -1.832f, 0.656f, -2.828f, 0.775f)
            curveToRelative(1.017f, -0.609f, 1.798f, -1.574f, 2.165f, -2.724f)
            curveToRelative(-0.951f, 0.564f, -2.005f, 0.974f, -3.127f, 1.195f)
            curveToRelative(-0.897f, -0.957f, -2.178f, -1.555f, -3.594f, -1.555f)
            curveToRelative(-3.179f, 0f, -5.515f, 2.966f, -4.797f, 6.045f)
            curveToRelative(-4.091f, -0.205f, -7.719f, -2.165f, -10.148f, -5.144f)
            curveToRelative(-1.29f, 2.213f, -0.669f, 5.108f, 1.523f, 6.574f)
            curveToRelative(-0.806f, -0.026f, -1.566f, -0.247f, -2.229f, -0.616f)
            curveToRelative(-0.054f, 2.281f, 1.581f, 4.415f, 3.949f, 4.89f)
            curveToRelative(-0.693f, 0.188f, -1.452f, 0.232f, -2.224f, 0.084f)
            curveToRelative(0.626f, 1.956f, 2.444f, 3.379f, 4.6f, 3.419f)
            curveToRelative(-2.07f, 1.623f, -4.678f, 2.348f, -7.29f, 2.04f)
            curveToRelative(2.179f, 1.397f, 4.768f, 2.212f, 7.548f, 2.212f)
            curveToRelative(9.142f, 0f, 14.307f, -7.721f, 13.995f, -14.646f)
            curveToRelative(0.962f, -0.695f, 1.797f, -1.562f, 2.457f, -2.549f)
            close()
        }
    }.also {
        cache = it
    }

private var cache: ImageVector? = null
