package sample.app

import androidx.compose.ui.geometry.Offset
import androidx.navigation3.runtime.NavKey
import com.feraxhp.navigation.CircularRevealKey
import kotlinx.serialization.Serializable

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
