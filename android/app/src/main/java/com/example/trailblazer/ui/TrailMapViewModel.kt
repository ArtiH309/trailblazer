package com.example.trailblazer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailblazer.net.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TrailPin(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double
)

sealed interface TrailUiState {
    data object Loading : TrailUiState
    data class Ready(val trails: List<TrailPin>) : TrailUiState
    data class Error(val message: String) : TrailUiState
}

class TrailMapViewModel : ViewModel() {

    private val _ui = MutableStateFlow<TrailUiState>(TrailUiState.Loading)
    val ui: StateFlow<TrailUiState> = _ui

    private var inFlight: Job? = null

    // keep a copy of whatever we last loaded so we can filter locally
    private var allTrails: List<TrailPin> = emptyList()

    // remember last map position / radius so we can refresh / clear search
    private var lastLat: Double = 40.7128
    private var lastLon: Double = -74.0060
    private var lastRadius: Double = 50.0

    init {
        // Initial fetch (NYC, 50km)
        refreshAt(lastLat, lastLon, lastRadius)
    }

    fun refreshAt(centerLat: Double, centerLon: Double, radiusKm: Double) {
        lastLat = centerLat
        lastLon = centerLon
        lastRadius = radiusKm

        inFlight?.cancel()
        inFlight = viewModelScope.launch {
            try {
                _ui.value = TrailUiState.Loading
                val near = "$centerLat,$centerLon"

                // These match your current ApiService
                val trails = ApiClient.service.getTrailsNearby(near, radiusKm)
                val parks = ApiClient.service.getParksNearby(near, radiusKm)

                val trailPins = trails.mapNotNull { t ->
                    val lat = t.lat ?: return@mapNotNull null
                    val lng = t.lng ?: return@mapNotNull null
                    TrailPin(t.id.toString(), t.name, lat, lng)
                }

                if (trailPins.isNotEmpty()) {
                    allTrails = trailPins
                    _ui.value = TrailUiState.Ready(trailPins)
                } else {
                    val parkPins = parks.map { p ->
                        TrailPin(
                            id = p.id.toString(),
                            name = p.name,
                            lat = p.lat,
                            lng = p.lng
                        )
                    }
                    allTrails = parkPins
                    _ui.value = TrailUiState.Ready(parkPins)
                }
            } catch (t: Throwable) {
                _ui.value = TrailUiState.Error(t.message ?: "Network error")
            }
        }
    }

    /**
     * Simple client-side filter on whatever trails we already loaded.
     */
    fun filterTrails(query: String) {
        if (query.isBlank()) {
            _ui.value = TrailUiState.Ready(allTrails)
            return
        }

        val filtered = allTrails.filter { trail ->
            trail.name.contains(query, ignoreCase = true)
        }
        _ui.value = TrailUiState.Ready(filtered)
    }

    /**
     * "Search" entry point used by the UI.
     * Since your API doesn't have /trails/search, we just filter locally.
     */
    fun searchTrails(query: String) {
        if (query.isBlank()) {
            clearSearch()
        } else {
            filterTrails(query)
        }
    }

    /**
     * Clear search and reload from last known map position.
     */
    fun clearSearch() {
        refreshAt(lastLat, lastLon, lastRadius)
    }
}
