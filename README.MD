# Navigation

*navigation* is a lightweight and concise Compose Multiplatform library wrapper around Jetpack Navigation 3, designed to provide ultra-clean boilerplate-free routing with built-in gestures and fluid transitions like circular reveal.

## Functionality
- Concise DSL navigation wrapper over Navigation 3 (`Navigator(HomeKey) { ... }`).
- Automatic `LocalNavigator` provider for child composables (`push`, `pop`, `replace`).
- Native Predictive Back support for Android (swipe gesture tracking with real-time finger progress).
- Circular Reveal & Unreveal transitions with dynamic touch origin coordinates (`circularEntry`).
- Support for Composable dynamic border colors (e.g. `MaterialTheme.colorScheme.primary`).
- Zero-configuration state persistence without manual polymorphic serialization boilerplate.


### Kotlin Multiplatform Support Maps

|             Platform              | Support |
|:---------------------------------:|:-------:|
|              Android              |    ✅    |
|              Desktop (JVM)        |    ✅    |
| iOSX64/iOSArm64/iosSimulatorArm64 |    ✅    |
|             WasmJs                |    ✅    |


## Configuration

1. Paste this code into your `libs.versions.toml` file:
```toml
[versions]

navigation = "0.1.0"

[libraries]

navigation = { module = "com.feraxhp.navigation:navigation", version.ref = "navigation" }
```

2. Then add the dependency into your `build.gradle.kts` in `kotlin > sourceSets > commonMain.dependencies`:
```kotlin
implementation(libs.navigation)
```

3. Sync Project with Gradle.

4. Define your keys (screens):
```kotlin
sealed class Screens : NavKey {
    @Serializable
    data object Home : Screens()

    @Serializable
    data class Details(
        val originX: Float,
        val originY: Float
    ) : Screens(), CircularRevealKey {
        override val origin: Offset
            get() = Offset(originX, originY)
    }
}
```

5. Call `Navigator` anywhere in your application:
```kotlin
import com.feraxhp.navigation.Navigator

@Composable
fun App() {
    Navigator(start = Screens.Home) {
        homeEntry()
        detailsEntry()
    }
}
```


## LocalNavigator Provider

`Navigator` automatically injects `LocalNavigator` into the composition hierarchy. Any child composable can access it:

```kotlin
val navigator = LocalNavigator.current

// Push screen to the stack
navigator.push(Screens.Details(originX = 200f, originY = 500f))

// Pop screen (only pops if stack size > 1)
navigator.pop()

// Replace top screen
navigator.replace(Screens.Home)
```


## Circular Reveal Transition

You can register any screen with an expanding circular reveal from the exact user tap position and smooth unreveal upon return:

```kotlin
fun EntryProviderScope<NavKey>.detailsEntry() {
    circularEntry<Screens.Details>(
        borderColor = { MaterialTheme.colorScheme.primary }, // Optional border ring
        borderWidth = 2.dp
    ) { key ->
        DetailsScreen()
    }
}
```

> [!TIP]
> If your `NavKey` implements `CircularRevealKey`, `circularEntry` extracts `origin` automatically. You can also pass an explicit lambda `origin = { key -> Offset(key.x, key.y) }` without implementing the interface.

To see a sample of the implementation you can check `:sample:sharedUI` under `commonMain`.
