package io.github.kei_1111

// import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import io.github.kei_1111.core.designsystem.theme.AppTheme
import io.github.kei_1111.ui.navigation.NavGraph

@Suppress("ModifierMissing")
@Composable
fun App() {
    val navController = rememberNavController()

    AppTheme(
        darkTheme = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            NavGraph(
                navController = navController,
            )
        }
    }
}

enum class DeviceType {
    Mobile, Desktop
}

val MobileWidth = 800.dp
