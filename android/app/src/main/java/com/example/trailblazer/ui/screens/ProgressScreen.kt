// FILE: android/app/src/main/java/com/example/trailblazer/ui/screens/ProgressScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailblazer.net.ApiClient
import com.example.trailblazer.net.ActivityDto
import kotlinx.coroutines.launch

@Composable
fun ProgressScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            activities = ApiClient.service.getMyActivities()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load activities"
        } finally {
            isLoading = false
        }
    }

    // Calculate stats
    val totalMiles = activities.sumOf { (it.distanceKm ?: 0.0) * 0.621371 }
    val totalHours = activities.sumOf { (it.durationMinutes ?: 0.0) / 60.0 }
    val totalElevation = activities.sumOf { it.elevationGainM ?: 0.0 }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(16.dp))

                    // Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            value = String.format("%.1f", totalMiles),
                            label = "Miles",
                            icon = Icons.Default.Terrain,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = String.format("%.1f", totalHours),
                            label = "Hours",
                            icon = Icons.Default.AccessTime,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = String.format("%.0f", totalElevation * 3.28084),
                            label = "Feet",
                            icon = Icons.Default.Landscape,
                            modifier = Modifier.weight(1f)
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
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            text = errorMessage ?: "Error loading activities",
                            fontSize = 16.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            } else if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Hiking,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Text(
                            text = "No activities yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "Start hiking to track your progress!",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Recent Activities",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(activities) { activity ->
                        ActivityCard(activity = activity)
                    }
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                currentScreen = "Progress",
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
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
}

@Composable
private fun ActivityCard(activity: ActivityDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Hiking,
                    null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trail #${activity.trailId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activity.distanceKm != null) {
                        Text(
                            text = String.format("%.1f mi", activity.distanceKm * 0.621371),
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    if (activity.durationMinutes != null) {
                        Text("•", fontSize = 13.sp, color = Color(0xFF757575))
                        Text(
                            text = formatDuration(activity.durationMinutes),
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Text(
                    text = formatDate(activity.date ?: activity.createdAt ?: ""),
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            // Elevation
            if (activity.elevationGainM != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        Icons.Default.Landscape,
                        null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = String.format("%.0f ft", activity.elevationGainM * 3.28084),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212121)
                    )
                }
            }
        }
    }
}

private fun formatDuration(minutes: Double): String {
    val hours = (minutes / 60).toInt()
    val mins = (minutes % 60).toInt()
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

private fun formatDate(timestamp: String): String {
    if (timestamp.isEmpty()) return ""

    return try {
        val date = timestamp.split("T")[0]
        val parts = date.split("-")
        if (parts.size == 3) {
            "${parts[1]}/${parts[2]}/${parts[0]}"
        } else {
            timestamp
        }
    } catch (e: Exception) {
        "Today"
    }
}

@Composable
private fun BottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavItem(
                icon = Icons.Default.LocationOn,
                label = "Map",
                isSelected = currentScreen == "Map",
                onClick = { onNavigate("Map") }
            )
            NavItem(
                icon = Icons.Default.Group,
                label = "Community",
                isSelected = currentScreen == "Community",
                onClick = { onNavigate("Community") }
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == "Profile",
                onClick = { onNavigate("Profile") }
            )
            NavItem(
                icon = Icons.Default.BarChart,
                label = "Progress",
                isSelected = currentScreen == "Progress",
                onClick = { onNavigate("Progress") }
            )
            NavItem(
                icon = Icons.Default.CloudDownload,
                label = "Offline",
                isSelected = currentScreen == "Offline",
                onClick = { onNavigate("Offline") }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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