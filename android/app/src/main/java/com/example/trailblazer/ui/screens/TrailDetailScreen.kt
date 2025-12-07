// FILE: app/src/main/java/com/example/trailblazer/ui/screens/TrailDetailScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun TrailDetailScreen(
    trailId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var trail by remember { mutableStateOf<TrailDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(trailId) {
        isLoading = true
        try {
            // In a real app, you'd fetch trail details
            // For now, we'll create a mock trail
            trail = TrailDto(
                id = trailId,
                name = "Trail #$trailId",
                difficulty = "Moderate",
                lengthKm = 3.2,
                elevationGainM = 150.0,
                lat = 40.7829,
                lng = -73.9654,
                accessible = true,
                hasWaterfall = false,
                hasViewpoint = true,
                avgRating = 4.5,
                ratingsCount = 120
            )
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
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
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Trail Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val result = ApiClient.service.toggleFavorite(trailId)
                            isFavorite = result.isFavorited
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                }) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val result = ApiClient.service.toggleOffline(trailId)
                            isOffline = result.isOffline
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                }) {
                    Icon(
                        if (isOffline) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                        "Offline",
                        tint = Color.White
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else if (trail != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trail Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = trail!!.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DifficultyChip(trail!!.difficulty ?: "Unknown")
                            if (trail!!.accessible == true) {
                                Chip("Accessible")
                            }
                            if (trail!!.hasViewpoint == true) {
                                Chip("Viewpoint")
                            }
                        }
                    }
                }

                // Stats Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Default.Terrain,
                            value = "${((trail!!.lengthKm ?: 0.0) * 0.621371).toInt()} mi",
                            label = "Distance"
                        )
                        StatItem(
                            icon = Icons.Default.Landscape,
                            value = "${trail!!.elevationGainM?.toInt() ?: 0} ft",
                            label = "Elevation"
                        )
                        StatItem(
                            icon = Icons.Default.Star,
                            value = String.format("%.1f", trail!!.avgRating),
                            label = "Rating"
                        )
                    }
                }

                // Action Buttons
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                ApiClient.service.logActivity(
                                    trailId = trailId,
                                    body = com.example.trailblazer.net.ActivityCreateRequest(
                                        distanceKm = trail!!.lengthKm,
                                        durationMinutes = 120.0
                                    )
                                )
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DirectionsWalk, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Activity")
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val color = when (difficulty.lowercase()) {
        "easy" -> Color(0xFF4CAF50)
        "moderate" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = difficulty,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun Chip(text: String) {
    Surface(
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF757575),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
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