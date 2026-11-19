package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.ui.components.VeridiaRoute

enum class DestinationPlaybackState {
    ACTIVE,
    PAUSED,
}

data class NavState(
    val currentRoute: String = VeridiaRoute.HOME,
    val previousRoute: String? = null,
    val isAppForeground: Boolean = true,
    val activeRoutes: Set<String> = setOf(VeridiaRoute.HOME),
    val pausedRoutes: Set<String> = emptySet(),
    val transitionCount: Int = 0,
) {
    fun isDestinationActive(route: String): Boolean =
        isAppForeground && currentRoute == route && route in activeRoutes

    fun playbackStateFor(route: String): DestinationPlaybackState =
        if (isDestinationActive(route)) DestinationPlaybackState.ACTIVE else DestinationPlaybackState.PAUSED
}

/**
 * Custom lifecycle-aware navigation animation state controller.
 *
 * Pauses Lottie / Compose animation frame ticks whenever a navigation destination becomes
 * non-visible (covered by another route or app backgrounded) and resumes them immediately
 * upon forward/back transition so backgrounded destinations produce zero frame-callback drain.
 */
@Stable
class NavStateController(
    initialRoute: String = VeridiaRoute.HOME,
    initialAppForeground: Boolean = true,
) {
    private val knownRoutes = mutableSetOf(initialRoute)
    private val destinationLifecycles = mutableMapOf<String, Lifecycle.State>()

    var navState by mutableStateOf(
        NavState(
            currentRoute = initialRoute,
            isAppForeground = initialAppForeground,
            activeRoutes = if (initialAppForeground) setOf(initialRoute) else emptySet(),
            pausedRoutes = if (initialAppForeground) emptySet() else setOf(initialRoute),
        )
    )
        private set

    fun onAppLifecycleChanged(state: Lifecycle.State) {
        val foreground = state.isAtLeast(Lifecycle.State.STARTED)
        recomputeState(
            currentRoute = navState.currentRoute,
            previousRoute = navState.previousRoute,
            isAppForeground = foreground,
            incrementTransition = false,
        )
    }

    fun onDestinationTransition(targetRoute: String) {
        if (targetRoute.isBlank()) return
        knownRoutes.add(targetRoute)
        val prev = navState.currentRoute.takeIf { it != targetRoute } ?: navState.previousRoute
        // Resume target immediately upon transition even before its backstack entry reaches RESUMED
        val existingState = destinationLifecycles[targetRoute]
        if (existingState == null || !existingState.isAtLeast(Lifecycle.State.STARTED)) {
            destinationLifecycles[targetRoute] = Lifecycle.State.STARTED
        }
        recomputeState(
            currentRoute = targetRoute,
            previousRoute = prev,
            isAppForeground = navState.isAppForeground,
            incrementTransition = navState.currentRoute != targetRoute,
        )
    }

    fun onDestinationLifecycleEvent(route: String, state: Lifecycle.State) {
        knownRoutes.add(route)
        destinationLifecycles[route] = state
        recomputeState(
            currentRoute = navState.currentRoute,
            previousRoute = navState.previousRoute,
            isAppForeground = navState.isAppForeground,
            incrementTransition = false,
        )
    }

    fun isDestinationAnimating(route: String): Boolean = navState.isDestinationActive(route)

    private fun recomputeState(
        currentRoute: String,
        previousRoute: String?,
        isAppForeground: Boolean,
        incrementTransition: Boolean,
    ) {
        val active = mutableSetOf<String>()
        val paused = mutableSetOf<String>()

        for (route in knownRoutes) {
            val entryLifecycle = destinationLifecycles[route]
            val entryVisible = entryLifecycle?.isAtLeast(Lifecycle.State.STARTED) ?: (route == currentRoute)
            if (isAppForeground && route == currentRoute && entryVisible) {
                active.add(route)
            } else {
                paused.add(route)
            }
        }

        val nextTransitionCount = if (incrementTransition) navState.transitionCount + 1 else navState.transitionCount
        val next = NavState(
            currentRoute = currentRoute,
            previousRoute = previousRoute,
            isAppForeground = isAppForeground,
            activeRoutes = active,
            pausedRoutes = paused,
            transitionCount = nextTransitionCount,
        )
        if (next != navState) {
            navState = next
        }
    }
}

val LocalNavStateController = compositionLocalOf<NavStateController?> { null }
val LocalDestinationRoute = compositionLocalOf<String?> { null }

@Composable
fun rememberNavStateController(
    navController: NavHostController,
    initialRoute: String = VeridiaRoute.HOME,
): NavStateController {
    val hostLifecycleOwner = LocalLifecycleOwner.current
    val initialForeground = hostLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val controller = remember(navController) {
        NavStateController(
            initialRoute = navController.currentBackStackEntry?.destination?.route ?: initialRoute,
            initialAppForeground = initialForeground,
        )
    }

    DisposableEffect(navController, controller) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                controller.onDestinationTransition(route)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    DisposableEffect(hostLifecycleOwner, controller) {
        controller.onAppLifecycleChanged(hostLifecycleOwner.lifecycle.currentState)
        val observer = LifecycleEventObserver { owner, _ ->
            controller.onAppLifecycleChanged(owner.lifecycle.currentState)
        }
        hostLifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            hostLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return controller
}

/**
 * Attaches a lifecycle observer to the current navigation destination's [LocalLifecycleOwner]
 * (typically [androidx.navigation.NavBackStackEntry]) and exposes [LocalDestinationRoute] so
 * child animations pause automatically when obscured or off-screen and resume on transition.
 */
@Composable
fun ProvideNavDestinationState(
    route: String,
    controller: NavStateController? = LocalNavStateController.current,
    content: @Composable () -> Unit,
) {
    val destinationLifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(destinationLifecycleOwner, route, controller) {
        controller?.onDestinationLifecycleEvent(route, destinationLifecycleOwner.lifecycle.currentState)
        val observer = LifecycleEventObserver { owner, _ ->
            controller?.onDestinationLifecycleEvent(route, owner.lifecycle.currentState)
        }
        destinationLifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            controller?.onDestinationLifecycleEvent(route, Lifecycle.State.CREATED)
            destinationLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalDestinationRoute provides route) {
        content()
    }
}

/**
 * Returns `true` only while the current navigation destination (and host lifecycle) is visible
 * and foregrounded. Composables should wrap continuous/infinite frame drivers in `if (isAnimating)`
 * so zero animation frames or Choreographer callbacks run when off-screen or backgrounded.
 */
@Composable
fun rememberIsDestinationAnimating(
    routeOverride: String? = null,
): Boolean {
    val controller = LocalNavStateController.current
    val scopedRoute = routeOverride ?: LocalDestinationRoute.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lifecycleStarted by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleStarted = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        val observer = LifecycleEventObserver { owner, _ ->
            lifecycleStarted = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isAnimating by remember(controller, scopedRoute) {
        derivedStateOf {
            if (!lifecycleStarted) {
                false
            } else if (controller != null && scopedRoute != null) {
                controller.isDestinationAnimating(scopedRoute)
            } else {
                true
            }
        }
    }
    return isAnimating
}
