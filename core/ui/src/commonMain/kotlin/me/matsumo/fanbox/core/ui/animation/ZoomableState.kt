package me.matsumo.fanbox.core.ui.animation

/*
 * Fork from https://github.com/mxalbert1996/Zoomable
 */

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Create a [ZoomableState] that is remembered across compositions.
 *
 * @param minScale The minimum [ZoomableState.scale] value.
 * @param maxScale The maximum [ZoomableState.scale] value.
 * @param doubleTapScale The [ZoomableState.scale] Value to animate to when a double tap happens.
 * @param overZoomConfig The [OverZoomConfig] to use or null to disable over-zoom effect.
 * @param initialScale The initial value for [ZoomableState.scale].
 * @param initialTranslationX The initial value for [ZoomableState.translationX].
 * @param initialTranslationY The initial value for [ZoomableState.translationY].
 */
@Composable
fun rememberZoomableState(
    minScale: Float = ZoomableDefaults.MIN_SCALE,
    maxScale: Float = ZoomableDefaults.MAX_SCALE,
    doubleTapScale: Float = ZoomableDefaults.DOUBLE_TAP_SCALE,
    overZoomConfig: OverZoomConfig? = null,
    initialScale: Float = minScale,
    initialTranslationX: Float = 0f,
    initialTranslationY: Float = 0f,
): ZoomableState {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val saver = remember(decayAnimationSpec) { ZoomableState.saver(decayAnimationSpec) }
    return rememberSaveable(decayAnimationSpec, saver = saver) {
        ZoomableState(decayAnimationSpec, initialScale, initialTranslationX, initialTranslationY)
    }.apply {
        this.minScale = minScale
        this.maxScale = maxScale
        this.doubleTapScale = doubleTapScale
        this.overZoomConfig = overZoomConfig
    }
}

/**
 * A state object that can be hoisted to observe scale and translate for [Zoomable].
 *
 * @param initialScale The initial value for [scale].
 * @param initialTranslationX The initial value for [translationX].
 * @param initialTranslationY The initial value for [translationY].
 * @see rememberZoomableState
 */
@Stable
// 拡大率・移動量・境界を同じ状態で更新し、gesture と animate/snap の整合を保つ。
@Suppress("TooManyFunctions")
class ZoomableState(
    private val decayAnimationSpec: DecayAnimationSpec<Float>,
    initialScale: Float = ZoomableDefaults.MIN_SCALE,
    initialTranslationX: Float = 0f,
    initialTranslationY: Float = 0f,
) {
    /**
     * The minimum [scale] value.
     */
    var minScale: Float = ZoomableDefaults.MIN_SCALE
        set(value) {
            if (field != value) {
                field = value
                scale = scale // Make sure scale is in range
            }
        }

    /**
     * The maximum [scale] value.
     */
    var maxScale: Float = ZoomableDefaults.MAX_SCALE
        set(value) {
            if (field != value) {
                field = value
                scale = scale // Make sure scale is in range
            }
        }

    var overZoomConfig: OverZoomConfig? by mutableStateOf(null)

    /**
     * The [scale] value to animate to when a double tap happens.
     */
    var doubleTapScale: Float = ZoomableDefaults.DOUBLE_TAP_SCALE

    /**
     * Current progress of the dismiss drag ranging from 0.0 to 1.0.
     * Useful e.g. if you want to animate the alpha of the content.
     */
    val dismissDragProgress: Float by derivedStateOf {
        if (size.height == 0) {
            0f
        } else {
            abs(dismissDragAbsoluteOffsetY) / (size.height * 0.25f)
        }
    }

    private val velocityTracker = VelocityTracker()
    private var _scale by mutableFloatStateOf(initialScale)
    private val translationXAnimation = Animatable(initialTranslationX, PX_VISIBILITY_THRESHOLD)
    private val translationYAnimation = Animatable(initialTranslationY, PX_VISIBILITY_THRESHOLD)
    private var sizeState by mutableStateOf(IntSize.Zero)
    private var childSizeState by mutableStateOf(Size.Zero)

    internal var boundOffset by mutableStateOf(IntOffset.Zero)
        private set

    internal var dismissDragAbsoluteOffsetY by mutableFloatStateOf(0f)
        private set

    internal val dismissDragOffsetY: Float by derivedStateOf {
        val maxOffset = childSize.height
        if (maxOffset == 0f) {
            0f
        } else {
            val progress = (dismissDragAbsoluteOffsetY / maxOffset).coerceIn(-1f, 1f)
            childSize.height / 2f * sin(progress * PI.toFloat() / 2)
        }
    }

    internal val shouldDismiss: Boolean
        get() = abs(dismissDragAbsoluteOffsetY) > size.height * 0.25f

    internal var size: IntSize
        get() = sizeState
        set(value) {
            if (sizeState != value) {
                sizeState = value
                updateBounds()
            }
        }

    internal var childSize: Size
        get() = childSizeState
        set(value) {
            if (childSizeState != value) {
                childSizeState = value
                updateBounds()
            }
        }

    /**
     * Current scale of [Zoomable].
     */
    var scale: Float
        get() = _scale
        private set(value) {
            _scale = value.coerceIn(minimumValue = minScale, maximumValue = maxScale)
            updateBounds()
        }

    /**
     * Current translationX of [Zoomable].
     */
    val translationX: Float
        get() = translationXAnimation.value

    /**
     * Current translationY of [Zoomable].
     */
    val translationY: Float
        get() = translationYAnimation.value

    internal val minSnapScale: Float
        get() = max(minScale, overZoomConfig?.minSnapScale ?: 0f)

    val isZooming: Boolean
        get() = scale > minSnapScale && scale <= maxScale

    private var flingJob: Job? = null
    internal var isGestureInProgress: Boolean by mutableStateOf(false)
        private set

    internal val horizontalEdge: HorizontalEdge
        get() = when {
            translationXAnimation.upperBound == translationXAnimation.lowerBound -> HorizontalEdge.Both
            translationXAnimation.run { value >= upperBound!! - PX_VISIBILITY_THRESHOLD } -> HorizontalEdge.Left
            translationXAnimation.run { value <= lowerBound!! + PX_VISIBILITY_THRESHOLD } -> HorizontalEdge.Right
            else -> HorizontalEdge.None
        }

    private fun updateBounds() {
        // If the child is an asynchronously loaded image, its size will be temporarily zero after
        // the translation values are restored. In that case we don't update the bounds to avoid
        // resetting the translation values.
        if (childSize == Size.Zero) return
        val offsetX = childSize.width * scale - size.width
        val offsetY = childSize.height * scale - size.height
        boundOffset = IntOffset((offsetX / 2f).roundToInt(), (offsetY / 2f).roundToInt())
        val maxX = offsetX.coerceAtLeast(0f) / 2f
        val maxY = offsetY.coerceAtLeast(0f) / 2f
        translationXAnimation.updateBounds(-maxX, maxX)
        translationYAnimation.updateBounds(-maxY, maxY)
    }

    internal fun calculateTargetTranslation(centroid: Offset): Offset = (size.toSize().center + Offset(translationX, translationY) - centroid) / scale

    /**
     * Animate [scale] to [targetScale].
     *
     * @param targetScale The [scale] value to animate to.
     * @param targetTranslation The [translationX] and [translationY] value to animate to. Use the
     * default value to maintain current center point. Use [Offset.Unspecified] to leave
     * translation unchanged.
     * @param animationSpec [AnimationSpec] to be used for this scaling.
     */
    suspend fun animateScaleTo(
        targetScale: Float,
        targetTranslation: Offset = Offset(translationX, translationY) / scale * targetScale,
        animationSpec: AnimationSpec<Float> = spring(),
    ) = coroutineScope {
        val initialTranslation = Offset(translationX, translationY)
        val initialScale = scale
        val range = targetScale - initialScale
        animate(
            initialValue = initialScale,
            targetValue = targetScale,
            animationSpec = animationSpec,
        ) { value, _ ->
            launch {
                // Update scale here to ensure scale and translation values are updated
                // in the same snapshot
                scale = value
                if (targetTranslation != Offset.Unspecified) {
                    val fraction = if (range == 0f) 1f else (value - initialScale) / range
                    val translation = lerp(initialTranslation, targetTranslation, fraction)
                    translationXAnimation.snapTo(translation.x)
                    translationYAnimation.snapTo(translation.y)
                }
            }
        }
    }

    /**
     * Animate [translationX] and [translationY] to [targetTranslation].
     *
     * @param targetTranslation The [translationX] and [translationY] value to animate to.
     * @param animationSpec [AnimationSpec] to be used for this scaling.
     */
    suspend fun animateTranslateTo(
        targetTranslation: Offset,
        animationSpec: AnimationSpec<Offset> = spring(),
    ) = coroutineScope {
        animate(
            typeConverter = Offset.VectorConverter,
            initialValue = Offset(translationX, translationY),
            targetValue = targetTranslation,
            animationSpec = animationSpec,
        ) { value, _ ->
            launch {
                translationXAnimation.snapTo(value.x)
                translationYAnimation.snapTo(value.y)
            }
        }
    }

    private suspend fun fling(velocity: Velocity) {
        coroutineScope {
            flingJob = coroutineContext[Job]
            launch {
                translationXAnimation.animateDecay(
                    initialVelocity = velocity.x,
                    animationSpec = decayAnimationSpec,
                )
            }
            launch {
                translationYAnimation.animateDecay(
                    initialVelocity = velocity.y,
                    animationSpec = decayAnimationSpec,
                )
            }
        }

        isGestureInProgress = false
        flingJob = null
    }

    internal fun onGestureStart() {
        flingJob?.cancel()
        isGestureInProgress = true
    }

    internal suspend fun onTransform(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
    ) {
        var targetTranslation = calculateTargetTranslation(centroid - pan)
        scale *= zoom
        targetTranslation = targetTranslation * scale - size.toSize().center + centroid
        translationXAnimation.snapTo(targetTranslation.x)
        translationYAnimation.snapTo(targetTranslation.y)
    }

    internal fun onTransformEnd() {
        isGestureInProgress = false
    }

    internal suspend fun onDrag(dragAmount: Offset) {
        translationXAnimation.snapTo(translationXAnimation.value + dragAmount.x)
        translationYAnimation.snapTo(translationYAnimation.value + dragAmount.y)
    }

    internal suspend fun onDragEnd() {
        val velocity = velocityTracker.calculateVelocity()
        velocityTracker.resetTracking()
        fling(velocity)
    }

    internal fun addPosition(timeMillis: Long, position: Offset) {
        velocityTracker.addPosition(timeMillis = timeMillis, position = position)
    }

    internal fun resetTracking() {
        velocityTracker.resetTracking()
    }

    internal fun onDismissDrag(dragAmountY: Float) {
        dismissDragAbsoluteOffsetY += dragAmountY
    }

    internal suspend fun onDismissDragEnd() {
        animate(
            initialValue = dismissDragAbsoluteOffsetY,
            targetValue = 0f,
        ) { value, _ ->
            dismissDragAbsoluteOffsetY = value
        }
    }

    override fun toString(): String = "ZoomableState(translateX=${translationX.roundToTenths()}, " +
        "translateY=${translationY.roundToTenths()}, scale=${scale.roundToTenths()})"

    companion object {
        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        fun saver(decayAnimationSpec: DecayAnimationSpec<Float>): Saver<ZoomableState, *> = listSaver(
            save = {
                listOf(
                    it.translationX,
                    it.translationY,
                    it.scale,
                )
            },
            restore = {
                ZoomableState(
                    decayAnimationSpec = decayAnimationSpec,
                    initialTranslationX = it[0],
                    initialTranslationY = it[1],
                    initialScale = it[2],
                )
            },
        )
    }
}

@kotlin.jvm.JvmInline
internal value class HorizontalEdge private constructor(private val value: Int) {

    fun isOutwards(direction: Float): Boolean {
        if (value and 0b10 != 0 && direction > 0) return true
        if (value and 0b01 != 0 && direction < 0) return true
        return false
    }

    companion object {
        val None = HorizontalEdge(0b00)
        val Left = HorizontalEdge(0b10)
        val Right = HorizontalEdge(0b01)
        val Both = HorizontalEdge(0b11)
    }
}

private const val PX_VISIBILITY_THRESHOLD = 0.5f

object ZoomableDefaults {
    /**
     * The default value for [ZoomableState.minScale].
     */
    const val MIN_SCALE = 1f

    /**
     * The default value for [ZoomableState.maxScale].
     */
    const val MAX_SCALE = 4f

    /**
     * The default value for [ZoomableState.doubleTapScale].
     */
    const val DOUBLE_TAP_SCALE = 2f
}

/**
 * Configuration for over-zoom effect.
 *
 * @property minSnapScale The minimum [ZoomableState.scale] value to snap to after a zoom gesture
 * finishes.
 * @property maxSnapScale The maximum [ZoomableState.scale] value to snap to after a zoom gesture
 * finishes.
 */
@Immutable
class OverZoomConfig(val minSnapScale: Float, val maxSnapScale: Float) {
    operator fun contains(scale: Float): Boolean = scale in minSnapScale..maxSnapScale

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OverZoomConfig) return false

        return minSnapScale == other.minSnapScale && maxSnapScale == other.maxSnapScale
    }

    override fun hashCode(): Int {
        var result = minSnapScale.hashCode()
        result = 31 * result + maxSnapScale.hashCode()
        return result
    }

    override fun toString(): String = "OverZoomConfig(${minSnapScale.roundToTenths()}..${maxSnapScale.roundToTenths()})"
}

internal val OverZoomConfig.range: ClosedFloatingPointRange<Float>
    get() = minSnapScale..maxSnapScale

private fun Float.roundToTenths(): Float {
    val shifted = this * 10
    val decimal = shifted - shifted.toInt()
    // Kotlin's round operator rounds 0.5f down to 0. Manually compare against
    // 0.5f and round up if necessary
    val roundedShifted = if (decimal >= 0.5f) {
        shifted.toInt() + 1
    } else {
        shifted.toInt()
    }
    return roundedShifted.toFloat() / 10
}
