package org.example.project

// import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import org.example.project.ui.navigation.NavGraph
import org.example.project.ui.theme.AppTheme

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

val MobileWidth = 600.dp
