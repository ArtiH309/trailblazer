// FILE: app/src/main/java/com/example/trailblazer/ui/screens/OfflineScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailblazer.net.ApiClient
import com.example.trailblazer.net.TrailDto
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.BarChart

@Composable
fun OfflineScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var offlineTrails by remember { mutableStateOf<List<TrailDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalSize by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    fun loadOfflineTrails() {
        scope.launch {
            isLoading = true
            try {
                offlineTrails = ApiClient.service.getOfflineTrails()
                // Simulate size calculation
                totalSize = offlineTrails.size * 5 // 5 MB per trail (simulated)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadOfflineTrails()
    }

    Column(
        modifier = modifier
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
                    text = "Offline Maps",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                // Storage Info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Storage,
                                    null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Storage Used",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { (totalSize / 500f).coerceAtMost(1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFFE0E0E0),
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalSize MB used",
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "${offlineTrails.size} trails",
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }

                // Info Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Download trails to access them without internet connection",
                                fontSize = 14.sp,
                                color = Color(0xFF1976D2),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Downloaded Trails Header
                item {
                    Text(
                        text = "Downloaded Trails",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                // Trail List
                if (offlineTrails.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFBDBDBD)
                                )
                                Text(
                                    text = "No offline trails",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "Download trails from the map to use offline",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                } else {
                    items(offlineTrails) { trail ->
                        OfflineTrailCard(
                            trail = trail,
                            onRemove = {
                                scope.launch {
                                    try {
                                        ApiClient.service.toggleOffline(trail.id)
                                        loadOfflineTrails()
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            currentScreen = "Offline",
            onNavigate = onNavigate
        )
    }
}
// ADD THIS CODE TO THE END OF OfflineScreen.kt
// (After the TrailCard function, just before the final closing brace)

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


// ALSO ADD THESE IMPORTS AT THE TOP IF MISSING:
@Composable
private fun OfflineTrailCard(
    trail: TrailDto,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
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
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CloudDone,
                    null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trail.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", (trail.lengthKm ?: 0.0) * 0.621371)} mi",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "~5 MB",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // Delete Button
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    "Remove",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}