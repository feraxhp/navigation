package sample.app

import androidx.compose.runtime.Composable
import com.feraxhp.navigation.Navigator
import sample.app.entries.detailsEntry
import sample.app.entries.homeEntry

@Composable
fun App() {
    Navigator(Screens.Home) {
        homeEntry()
        detailsEntry()
    }
}
