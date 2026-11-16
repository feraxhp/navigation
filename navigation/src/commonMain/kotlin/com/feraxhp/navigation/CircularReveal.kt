package com.feraxhp.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlin.math.hypot
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Interface that can be implemented by any custom [NavKey] to support circular reveal transition.
 */
interface CircularRevealKey : NavKey {
    val origin: Offset?
}

/**
 * Creates [NavDisplay] metadata that keeps background visible while fading out,
 * designed to be combined with [CircularRevealContainer].
 */
fun circularRevealMetadata(
    durationMillis: Int = 1000
): Map<String, Any> {
    val transition = EnterTransition.None togetherWith fadeOut(
        tween(durationMillis, easing = EaseInExpo)
    )
    return NavDisplay.transitionSpec { transition } +
        NavDisplay.popTransitionSpec { transition } +
        NavDisplay.predictivePopTransitionSpec { transition }
}

/**
 * DSL extension to register a screen implementing [CircularRevealKey] with circular reveal and predictive unreveal.
 */
inline fun <reified K : CircularRevealKey> EntryProviderScope<NavKey>.circularEntry(
    noinline borderColor: @Composable () -> Color? = { null },
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    crossinline content: @Composable (K) -> Unit
) {
    circularEntry(
        keyExtractor = { it },
        origin = { it.origin },
        borderColor = borderColor,
        borderWidth = borderWidth,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        content = content
    )
}

/**
 * Overload taking a static [Color] directly.
 */
inline fun <reified K : CircularRevealKey> EntryProviderScope<NavKey>.circularEntry(
    borderColor: Color?,
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    crossinline content: @Composable (K) -> Unit
) {
    circularEntry(
        borderColor = { borderColor },
        borderWidth = borderWidth,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        content = content
    )
}

/**
 * DSL extension to register any custom [NavKey] with circular reveal and predictive unreveal using an origin lambda.
 */
inline fun <reified K : NavKey> EntryProviderScope<NavKey>.circularEntry(
    noinline origin: (K) -> Offset?,
    noinline borderColor: @Composable () -> Color? = { null },
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    crossinline content: @Composable (K) -> Unit
) {
    circularEntry(
        keyExtractor = { it },
        origin = origin,
        borderColor = borderColor,
        borderWidth = borderWidth,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        content = content
    )
}

/**
 * Overload taking a static [Color] directly.
 */
inline fun <reified K : NavKey> EntryProviderScope<NavKey>.circularEntry(
    noinline origin: (K) -> Offset?,
    borderColor: Color?,
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    crossinline content: @Composable (K) -> Unit
) {
    circularEntry(
        origin = origin,
        borderColor = { borderColor },
        borderWidth = borderWidth,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        content = content
    )
}

@PublishedApi
internal inline fun <reified K : NavKey> EntryProviderScope<NavKey>.circularEntry(
    noinline keyExtractor: (K) -> NavKey,
    noinline origin: (K) -> Offset?,
    noinline borderColor: @Composable () -> Color? = { null },
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    crossinline content: @Composable (K) -> Unit
) {
    entry<K>(
        metadata = circularRevealMetadata()
    ) { key ->
        val offset = origin(key)
        if (offset != null) {
            val resolvedBorderColor = borderColor()
            CircularRevealContainer(
                navKey = keyExtractor(key),
                origin = offset,
                borderColor = resolvedBorderColor,
                borderWidth = borderWidth,
                enterAnimationSpec = enterAnimationSpec,
                exitAnimationSpec = exitAnimationSpec
            ) {
                content(key)
            }
        } else {
            content(key)
        }
    }
}

/**
 * Container that automatically manages:
 * 1. Circular reveal (expansion) on screen entry.
 * 2. Predictive Back tracking in real time (closing circle with finger gesture progress).
 * 3. Deterministic detection of whether the gesture was cancelled or confirmed via [LocalNavigator].
 * 4. Standard non-predictive pop() unreveal via lifecycle.
 */
@Composable
fun CircularRevealContainer(
    origin: Offset,
    borderColor: Color? = null,
    borderWidth: Dp = 2.dp,
    enterAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseInCirc),
    exitAnimationSpec: AnimationSpec<Float> = tween(800, easing = EaseOutCubic),
    navKey: NavKey? = null,
    content: @Composable () -> Unit
) {
    // -1f denotes uninitialized (initial entry expansion pending or active)
    val swipeProgress = remember { Animatable(-1f) }
    val backStack = LocalNavigator.current

    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current
    val dispatcher = dispatcherOwner?.navigationEventDispatcher

    val transitionState by dispatcher
        ?.transitionState
        ?.collectAsState(initial = NavigationEventTransitionState.Idle)
        ?: remember { mutableStateOf(NavigationEventTransitionState.Idle) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val isGestureDriven = remember { mutableStateOf(false) }

    // 1. Handle standard back navigation and screen pushes via lifecycle
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                if (swipeProgress.value != -1f && !isGestureDriven.value) {
                    swipeProgress.snapTo(-1f)
                }
            }
            Lifecycle.State.CREATED -> {
                // Standard pop() (not predictive swipe): animate circle closed
                if (!isGestureDriven.value && swipeProgress.value == -1f) {
                    swipeProgress.snapTo(1f)
                    swipeProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = exitAnimationSpec
                    )
                }
            }
            else -> {}
        }
    }

    // 2. Handle Predictive Back gestures tracking the finger position in real-time
    LaunchedEffect(transitionState) {
        when (val state = transitionState) {
            is NavigationEventTransitionState.InProgress -> {
                isGestureDriven.value = true
                val progress = state.latestEvent.progress
                // Contract circle from 1f down to 0f as user swipes
                swipeProgress.snapTo((1f - progress).coerceIn(0f, 1f))
            }
            is NavigationEventTransitionState.Idle -> {
                if (isGestureDriven.value) {
                    // Yield briefly so NavigationBackHandler's onBackCompleted (backStack.pop()) executes if confirmed
                    delay(16.milliseconds)

                    val isStillOnTop = if (navKey != null) {
                        backStack.lastOrNull() == navKey
                    } else {
                        lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                    }

                    if (isStillOnTop) {
                        // Gesture was CANCELLED: skip animation and snap immediately to 100% visible!
                        swipeProgress.snapTo(-1f)
                    } else {
                        // Gesture was CONFIRMED: close the remaining circle smoothly
                        swipeProgress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(300, easing = EaseOutCubic)
                        )
                    }
                    isGestureDriven.value = false
                }
            }
        }
    }

    val externalProgress = if (swipeProgress.value == -1f) null else swipeProgress.value

    CircularRevealInternal(
        origin = origin,
        progress = externalProgress,
        borderColor = borderColor,
        borderWidth = borderWidth,
        animationSpec = enterAnimationSpec,
        content = content
    )
}

@Composable
internal fun CircularRevealInternal(
    origin: Offset,
    progress: Float?,
    borderColor: Color?,
    borderWidth: Dp,
    animationSpec: AnimationSpec<Float>,
    content: @Composable () -> Unit
) {
    val internalProgress = remember { Animatable(0f) }

    if (progress == null) {
        LaunchedEffect(origin) {
            // Only run the entry animation once (when internalProgress is still 0f)
            if (internalProgress.value == 0f) {
                internalProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec
                )
            }
        }
    }

    val currentProgress = progress ?: internalProgress.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                // If progress is null and initial reveal finished, draw content directly with 0 clipping
                if (progress == null && internalProgress.value >= 1f) {
                    this@drawWithContent.drawContent()
                    return@drawWithContent
                }

                val maxRadius = hypot(
                    max(origin.x, size.width - origin.x),
                    max(origin.y, size.height - origin.y)
                )
                val currentRadius = maxRadius * currentProgress

                clipPath(Path().apply {
                    addOval(Rect(origin, currentRadius))
                }) {
                    this@drawWithContent.drawContent()
                }

                if (borderColor != null && currentProgress in 0.001f..0.999f) {
                    drawCircle(
                        color = borderColor,
                        radius = currentRadius,
                        center = origin,
                        style = Stroke(width = borderWidth.toPx())
                    )
                }
            }
    ) {
        content()
    }
}
