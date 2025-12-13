// FILE: android/app/src/main/java/com/example/trailblazer/ui/screens/ProfileScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailblazer.net.ApiClient
import com.example.trailblazer.net.AuthStore
import com.example.trailblazer.net.ProfileDto
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var profile by remember { mutableStateOf<ProfileDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            profile = ApiClient.service.getMyProfile()
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Top Bar
            Surface(
                color = Color(0xFF4CAF50),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (profile?.displayName?.firstOrNull() ?: "H").toString().uppercase(),
                                        color = Color.White,
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = profile?.displayName ?: "Hiker",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )

                                if (profile?.homeState != null) {
                                    Text(
                                        text = profile!!.homeState!!,
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                }

                                if (profile?.bio != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = profile!!.bio!!,
                                        fontSize = 14.sp,
                                        color = Color(0xFF616161)
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = onEditProfile,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit Profile")
                                }
                            }
                        }
                    }

                    // Stats Card
                    item {
                        Text(
                            text = "Your Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    icon = Icons.Default.Terrain,
                                    value = String.format("%.1f", (profile?.totalDistanceKm ?: 0.0) * 0.621371),
                                    label = "Miles",
                                    color = Color(0xFF4CAF50)
                                )
                                StatItem(
                                    icon = Icons.Default.Landscape,
                                    value = "${profile?.totalTrailsCompleted ?: 0}",
                                    label = "Trails",
                                    color = Color(0xFF2196F3)
                                )
                                StatItem(
                                    icon = Icons.Default.EmojiEvents,
                                    value = "8",
                                    label = "Badges",
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }

                    // Achievements
                    item {
                        Text(
                            text = "Achievements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AchievementItem(
                                    icon = "🏆",
                                    title = "First Steps",
                                    description = "Complete your first trail",
                                    unlocked = true
                                )
                                AchievementItem(
                                    icon = "🥾",
                                    title = "Trail Blazer",
                                    description = "Hike 10 different trails",
                                    unlocked = true
                                )
                                AchievementItem(
                                    icon = "⛰️",
                                    title = "Peak Seeker",
                                    description = "Climb 5,000 feet total",
                                    unlocked = true
                                )
                                AchievementItem(
                                    icon = "🌟",
                                    title = "Century Club",
                                    description = "Complete 100 miles",
                                    unlocked = false,
                                    progress = String.format("%.1f/100 miles", (profile?.totalDistanceKm ?: 0.0) * 0.621371)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavItem(
                        icon = Icons.Default.Map,
                        label = "Map",
                        isSelected = false,
                        onClick = { onNavigate("Map") }
                    )
                    NavItem(
                        icon = Icons.Default.People,
                        label = "Community",
                        isSelected = false,
                        onClick = { onNavigate("Community") }
                    )
                    NavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        isSelected = true,
                        onClick = { onNavigate("Profile") }
                    )
                    NavItem(
                        icon = Icons.Default.Timeline,
                        label = "Progress",
                        isSelected = false,
                        onClick = { onNavigate("Progress") }
                    )
                    NavItem(
                        icon = Icons.Default.CloudDownload,
                        label = "Offline",
                        isSelected = false,
                        onClick = { onNavigate("Offline") }
                    )
                }
            }
        }

        // Settings Dialog - WITH WORKING BUTTONS
        if (showSettings) {
            SettingsDialog(
                onDismiss = { showSettings = false },
                onLogout = { showLogoutConfirm = true }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = {
                    Text("Logout", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Are you sure you want to logout?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Clear auth token
                            AuthStore.token = null

                            showLogoutConfirm = false
                            showSettings = false

                            // Navigate to login
                            onNavigate("Login")
                        }
                    ) {
                        Text("Logout", color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("Cancel", color = Color(0xFF757575))
                    }
                }
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun AchievementItem(
    icon: String,
    title: String,
    description: String,
    unlocked: Boolean,
    progress: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) Color(0xFFFFD700) else Color(0xFFE0E0E0)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) Color(0xFF212121) else Color(0xFF9E9E9E)
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
            if (progress != null) {
                Text(
                    text = progress,
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (unlocked) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Settings",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = { /* TODO: Implement notifications settings */ }
                )
                SettingItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy",
                    onClick = { /* TODO: Implement privacy settings */ }
                )
                SettingItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    onClick = { /* TODO: Implement language settings */ }
                )
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    onClick = { /* TODO: Implement about screen */ }
                )
                HorizontalDivider()
                SettingItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    color = Color(0xFFD32F2F),
                    onClick = onLogout
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF4CAF50))
            }
        }
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    color: Color = Color(0xFF212121),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = color
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        )
    }
}