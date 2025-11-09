// FILE: app/src/main/java/com/example/trailblazer/ui/screens/HomeMapScreen.kt
package com.example.trailblazer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailblazer.ui.TrailMapViewModel
import com.example.trailblazer.ui.TrailUiState
import com.example.trailblazer.ui.rememberMapViewWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@SuppressLint("MissingPermission")
@Composable
fun HomeMapScreen(
    vm: TrailMapViewModel = viewModel(),
    onTrailClick: (Int) -> Unit,
    onNavigateToScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.ui.collectAsState()
    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        // Map Background
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { gMap ->
                        googleMap = gMap
                        gMap.uiSettings.isZoomControlsEnabled = false
                        gMap.uiSettings.isMapToolbarEnabled = false
                        try { gMap.isMyLocationEnabled = true } catch (_: SecurityException) {}

                        // Initial camera position (NYC)
                        gMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(40.7128, -74.0060), 11f)
                        )
                    }
                }
            },
            update = {
                val gMap = googleMap ?: return@AndroidView
                when (val ui = state) {
                    is TrailUiState.Ready -> {
                        gMap.clear()
                        ui.trails.forEach { pin ->
                            gMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(pin.lat, pin.lng))
                                    .title(pin.name)
                            )
                        }
                    }
                    else -> Unit
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar with Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Home/Map",
                fontSize = 16.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search trails...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color(0xFF757575))
                },
                trailingIcon = {
                    Icon(Icons.Default.FilterList, null, tint = Color(0xFF757575))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Bottom Trail Card
        when (val ui = state) {
            is TrailUiState.Ready -> {
                if (ui.trails.isNotEmpty()) {
                    val trail = ui.trails.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 80.dp)
                            .clickable { onTrailClick(trail.id.toInt()) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Trail Image
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Trail Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = trail.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                                Text(
                                    text = "3.2 miles • Moderate",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575)
                                )
                            }

                            // View Button
                            Button(
                                onClick = { onTrailClick(trail.id.toInt()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("View Details", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            else -> Unit
        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            currentScreen = "Map",
            onNavigate = onNavigateToScreen,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Default.LocationOn,
                label = "Map",
                isSelected = currentScreen == "Map",
                onClick = { onNavigate("Map") }
            )
            BottomNavItem(
                icon = Icons.Default.Group,
                label = "Community",
                isSelected = currentScreen == "Community",
                onClick = { onNavigate("Community") }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == "Profile",
                onClick = { onNavigate("Profile") }
            )
            BottomNavItem(
                icon = Icons.Default.BarChart,
                label = "Progress",
                isSelected = currentScreen == "Progress",
                onClick = { onNavigate("Progress") }
            )
            BottomNavItem(
                icon = Icons.Default.CloudDownload,
                label = "Offline",
                isSelected = currentScreen == "Offline",
                onClick = { onNavigate("Offline") }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}