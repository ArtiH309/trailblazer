// FILE: android/app/src/main/java/com/example/trailblazer/MainActivity.kt
package com.example.trailblazer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("Register") }

    when (currentScreen) {
        "Register" -> RegisterScreen(
            onRegisterClick = { name, email, pass ->
                // TODO: Call your backend API here
                println("Register: $name, $email")
                currentScreen = "Map"
            },
            onSignInClick = {
                println("Sign in clicked")
            },
            onGoogleClick = {
                println("Google sign in clicked")
            },
            onAppleClick = {
                println("Apple sign in clicked")
            }
        )

        "Map" -> HomeMapScreen(
            onTrailClick = { trailId ->
                println("Trail clicked: $trailId")
            },
            onNavigateToScreen = { screen ->
                currentScreen = screen
            }
        )

        "Community" -> CommunityScreen(
            onNavigate = { screen -> currentScreen = screen }
        )

        "Profile" -> ProfileScreen(
            onNavigate = { screen -> currentScreen = screen },
            onEditProfile = {
                println("Edit profile clicked")
            }
        )

        "Progress" -> ProgressScreen(
            onNavigate = { screen -> currentScreen = screen }
        )

        "Offline" -> OfflineScreen(
            onNavigate = { screen -> currentScreen = screen }
        )
    }
}