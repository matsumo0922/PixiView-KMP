package me.matsumo.fanbox.core.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

object NavigateAnimation {
    val decelerateEasing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

    object Home {
        val enter = fadeIn(tween(240)) + scaleIn(
            initialScale = 0.92f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(240, 0, decelerateEasing),
        )

        val exit = fadeOut(tween(240))
    }

    object Fade {
        private const val DURATION_FADE = 180

        val enter = fadeIn(tween(DURATION_FADE))
        val popExit = fadeOut(tween(DURATION_FADE))
        val popEnter = fadeIn(tween(DURATION_FADE))
        val exit = fadeOut(tween(DURATION_FADE))
    }

    object Vertical {
        private const val DURATION_FADE = 180
        private const val DURATION_SLIDE = 180

        val enter = fadeIn(tween(DURATION_FADE)) + slideIn(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            initialOffset = { IntOffset(0, (-it.height * 0.1).toInt()) },
        )

        val popExit = fadeOut(tween(DURATION_FADE)) + slideOut(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            targetOffset = { IntOffset(0, (-it.height * 0.1).toInt()) },
        )

        val popEnter = fadeIn(tween(DURATION_FADE)) + slideIn(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            initialOffset = { IntOffset(0, (it.height * 0.1).toInt()) },
        )

        val exit = fadeOut(tween(DURATION_FADE)) + slideOut(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            targetOffset = { IntOffset(0, (it.height * 0.1).toInt()) },
        )
    }

    object Horizontal {
        private const val DURATION_FADE = 300
        private const val DURATION_SLIDE = 300

        val enter = fadeIn(tween(DURATION_FADE)) + slideIn(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            initialOffset = { IntOffset((it.width * 0.1).toInt(), 0) },
        )

        val popExit = fadeOut(tween(DURATION_FADE)) + slideOut(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            targetOffset = { IntOffset((it.width * 0.1).toInt(), 0) },
        )

        val popEnter = fadeIn(tween(DURATION_FADE)) + slideIn(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            initialOffset = { IntOffset((-it.width * 0.1).toInt(), 0) },
        )

        val exit = fadeOut(tween(DURATION_FADE)) + slideOut(
            animationSpec = tween(DURATION_SLIDE, 0, decelerateEasing),
            targetOffset = { IntOffset((-it.width * 0.1).toInt(), 0) },
        )
    }
}