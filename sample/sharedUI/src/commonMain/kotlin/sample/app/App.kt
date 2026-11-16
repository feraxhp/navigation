package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import com.feraxhp.navigation.LocalNavigator
import com.feraxhp.navigation.Navigator
import com.feraxhp.navigation.pop
import com.feraxhp.navigation.push
import kotlinx.serialization.Serializable

sealed class Screens : NavKey {
    @Serializable
    data object Home : Screens()

    @Serializable
    data object Details : Screens()
}

@Composable
fun App() {
    Navigator(Screens.Home) {
        entry<Screens.Home> {
            val navigator = LocalNavigator.current
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable { navigator.push(Screens.Details) },
                contentAlignment = Alignment.Center
            ) {
                BasicText("Home (tap to go to Details)")
            }
        }
        entry<Screens.Details> {
            val navigator = LocalNavigator.current
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Cyan)
                    .clickable { navigator.pop() },
                contentAlignment = Alignment.Center
            ) {
                BasicText("Details (tap to pop back)")
            }
        }
    }
}
