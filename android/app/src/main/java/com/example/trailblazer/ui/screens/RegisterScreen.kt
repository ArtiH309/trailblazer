// FILE: app/src/main/java/com/example/trailblazer/ui/screens/RegisterScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailblazer.net.ApiClient
import com.example.trailblazer.net.AuthStore
import com.example.trailblazer.net.LoginRequest
import com.example.trailblazer.net.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onSignInClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleMessage by remember { mutableStateOf(false) }
    var showAppleMessage by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF4CAF50), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = "TrailBlazer",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        Text(
            text = "Create an account",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(40.dp))

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(Modifier.height(12.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Create Account Button
        Button(
            onClick = {
                errorMessage = null
                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val api = ApiClient.service
                        // Register user
                        api.register(
                            RegisterRequest(
                                email = email,
                                password = password,
                                displayName = fullName
                            )
                        )
                        // Login to get token
                        val auth = api.login(
                            LoginRequest(email = email, password = password)
                        )
                        AuthStore.token = auth.accessToken
                        // Let the parent know registration succeeded
                        onRegisterClick(fullName, email, password)
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Something went wrong. Please try again."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Sign In Link
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = "Sign In",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSignInClick() }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Continue with Google - WITH FEEDBACK
        OutlinedButton(
            onClick = {
                showGoogleMessage = true
                onGoogleClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Continue with Google",
                fontSize = 15.sp,
                color = Color(0xFF212121)
            )
        }

        if (showGoogleMessage) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Google Sign-In coming soon!",
                fontSize = 12.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(12.dp))

        // Continue with Apple - WITH FEEDBACK
        Button(
            onClick = {
                showAppleMessage = true
                onAppleClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Continue with Apple",
                fontSize = 15.sp
            )
        }

        if (showAppleMessage) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Apple Sign-In coming soon!",
                fontSize = 12.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(24.dp))

        // Terms
        Text(
            text = "By continuing, you agree to our Terms of Service and\nPrivacy Policy",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}