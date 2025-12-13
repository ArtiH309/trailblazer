// FILE: android/app/src/main/java/com/example/trailblazer/MainActivity.kt
package com.example.trailblazer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.trailblazer.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}

private fun screenFromName(name: String): Screen = when (name) {
    "Login" -> Screen.Login
    "Register" -> Screen.Register
    "Map" -> Screen.Map
    "Community" -> Screen.Community
    "Profile" -> Screen.Profile
    "Progress" -> Screen.Progress
    "Offline" -> Screen.Offline
    else -> Screen.Map
}

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Map : Screen()
    object Community : Screen()
    object Profile : Screen()
    object Progress : Screen()
    object Offline : Screen()
    data class TrailDetail(val trailId: Int) : Screen()
    object EditProfile : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    val mapVm: com.example.trailblazer.ui.TrailMapViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(com.example.trailblazer.net.AuthStore.token) {
        val token = com.example.trailblazer.net.AuthStore.token
        val isAuthScreen = currentScreen is Screen.Login || currentScreen is Screen.Register
        if (token == null && !isAuthScreen) {
            currentScreen = Screen.Login
        }
    }

    when (val screen = currentScreen) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = {
                currentScreen = Screen.Map
            },
            onNavigateToRegister = {
                currentScreen = Screen.Register
            }
        )

        is Screen.Register -> RegisterScreen(
            onRegisterClick = { name, email, pass ->
                println("Registered user: $name ($email)")
                currentScreen = Screen.Map
            },
            onSignInClick = {
                currentScreen = Screen.Login
            },
            onGoogleClick = {
                println("Google sign in clicked (not implemented)")
            },
            onAppleClick = {
                println("Apple sign in clicked (not implemented)")
            }
        )

        is Screen.Map -> HomeMapScreen(
            vm = mapVm,
            onTrailClick = { trailId ->
                println("Trail clicked: $trailId")
                currentScreen = Screen.TrailDetail(trailId)
            },
            onNavigateToScreen = { screenName ->
                 currentScreen = screenFromName(screenName)
            }
        )

        is Screen.Community -> CommunityScreen(
            onNavigate = { screenName ->
                currentScreen = screenFromName(screenName)
            }
        )

        is Screen.Profile -> ProfileScreen(
            onNavigate = { screenName ->
                currentScreen = screenFromName(screenName)
            },
            onEditProfile = {
                currentScreen = Screen.EditProfile
            }
        )

        is Screen.Progress -> ProgressScreen(
            onNavigate = { screenName ->
                currentScreen = screenFromName(screenName)
            }
        )

        is Screen.Offline -> OfflineScreen(
            onNavigate = { screenName ->
                currentScreen = screenFromName(screenName)
            }
        )

        is Screen.TrailDetail -> TrailDetailScreen(
            trailId = screen.trailId,
            onBack = {
                mapVm.clearSearch()
                currentScreen = Screen.Map
            }
        )

        is Screen.EditProfile -> EditProfileScreen(
            onBack = {
                currentScreen = Screen.Profile
            },
            onSave = {
                currentScreen = Screen.Profile
            }
        )
    }
}