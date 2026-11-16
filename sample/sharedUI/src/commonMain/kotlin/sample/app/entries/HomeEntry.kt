package sample.app.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.feraxhp.navigation.LocalNavigator
import com.feraxhp.navigation.push
import sample.app.Screens

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<Screens.Home> {
        val navigator = LocalNavigator.current
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE))
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Dynamically passes the exact touch position to the NavKey
                        navigator.push(
                            Screens.Details(
                                originX = tapOffset.x,
                                originY = tapOffset.y
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            BasicText("Home Screen\n\n👉 Tap anywhere on screen! 👈\nThe circle reveal expands from your exact finger position.")
        }
    }
}
