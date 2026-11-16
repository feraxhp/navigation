package sample.app.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.feraxhp.navigation.LocalNavigator
import com.feraxhp.navigation.circularEntry
import com.feraxhp.navigation.pop
import sample.app.Screens

fun EntryProviderScope<NavKey>.detailsEntry() {
    // Look how easy it is now to pass MaterialTheme.colorScheme.primary!
    circularEntry<Screens.Details>(
        borderColor = { MaterialTheme.colorScheme.primary }
    ) { key ->
        val navigator = LocalNavigator.current
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF26A69A))
                .clickable { navigator.pop() },
            contentAlignment = Alignment.Center
        ) {
            BasicText("Details Screen!\nOrigin: (${key.originX.toInt()}, ${key.originY.toInt()})\nBorder: MaterialTheme.colorScheme.primary\nTap to pop back")
        }
    }
}
