package com.feraxhp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

val LocalNavigator = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("No LocalNavigator provided. Ensure Navigator { ... } is at the root.")
}

/**
 * Standard Navigator composable that requires no configuration or serializer registration.
 *
 * Example usage:
 * ```kotlin
 * Navigator(HomeKey) {
 *     entry<HomeKey> { HomeScreen() }
 *     entry<ProfileKey> { key -> ProfileScreen(key.id) }
 * }
 * ```
 */
@Composable
fun Navigator(
    start: NavKey,
    modifier: Modifier = Modifier,
    onBack: (NavBackStack<NavKey>.() -> Unit)? = null,
    entries: EntryProviderScope<NavKey>.() -> Unit
) {
    val navigator = rememberNavBackStack(start)
    Navigator(
        backStack = navigator,
        modifier = modifier,
        onBack = onBack,
        entries = entries
    )
}

/**
 * Overload of [Navigator] when the caller provides an existing [NavBackStack].
 */
@Composable
fun Navigator(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    onBack: (NavBackStack<NavKey>.() -> Unit)? = null,
    entries: EntryProviderScope<NavKey>.() -> Unit
) {
    CompositionLocalProvider(LocalNavigator provides backStack) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                if (onBack != null) {
                    backStack.onBack()
                } else {
                    backStack.pop()
                }
            },
            entryProvider = entryProvider(builder = entries)
        )
    }
}

/**
 * Remembers a [NavBackStack] in the current composition hierarchy.
 */
@Composable
fun rememberNavBackStack(start: NavKey): NavBackStack<NavKey> {
    return remember {
        NavBackStack(mutableStateListOf(start))
    }
}

/**
 * Pushes a new screen onto the back stack.
 */
fun NavBackStack<NavKey>.push(screen: NavKey) {
    add(screen)
}

/**
 * Pops the top screen from the back stack if there is more than 1 item.
 */
fun NavBackStack<NavKey>.pop(): Boolean {
    if (size <= 1) return false
    return removeLastOrNull() != null
}

/**
 * Replaces the top screen with [screen].
 */
fun NavBackStack<NavKey>.replace(screen: NavKey) {
    if (isNotEmpty()) {
        removeLastOrNull()
    }
    add(screen)
}
