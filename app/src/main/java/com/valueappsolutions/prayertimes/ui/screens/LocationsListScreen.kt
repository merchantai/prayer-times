package com.valueappsolutions.prayertimes.ui.screens

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valueappsolutions.prayertimes.domain.SavedLocation
import com.valueappsolutions.prayertimes.ui.theme.MetallicGold
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerUiState
import com.valueappsolutions.prayertimes.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsListScreen(
    viewModel: PrayerViewModel,
    onNavigateToDetail: (Double, Double, String, String?) -> Unit
) {
    val savedLocations by viewModel.savedLocations.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var cityName by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locations", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        val currentLoc = currentLocation
                        if (currentLoc != null) {
                            val currentName = if (uiState is PrayerUiState.Success) {
                                val data = (uiState as PrayerUiState.Success).prayerData
                                if (data.cityName.isNotBlank() && data.areaName.isNotBlank()) {
                                    "${data.areaName}, ${data.cityName}"
                                } else if (data.cityName.isNotBlank()) {
                                    data.cityName
                                } else {
                                    "Current Location"
                                }
                            } else {
                                "Current Location"
                            }
                            val newLoc = SavedLocation(
                                id = UUID.randomUUID().toString(),
                                name = currentName,
                                latitude = currentLoc.latitude,
                                longitude = currentLoc.longitude,
                                timezoneId = if (uiState is PrayerUiState.Success) (uiState as PrayerUiState.Success).prayerData.destinationTimezoneId else ""
                            )
                            viewModel.saveLocation(newLoc)
                            Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Current location not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Current")
                }

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New")
                }
            }

            // List of saved locations
            if (savedLocations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved locations yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedLocations) { location ->
                        LocationCard(
                            location = location,
                            onClick = { onNavigateToDetail(location.latitude, location.longitude, location.name, location.timezoneId) },
                            onSetCurrent = {
                                viewModel.setAsCurrentLocation(location)
                                Toast.makeText(context, "${location.name} set as current location", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.removeLocation(location.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSearching) {
                    showAddDialog = false
                    searchResults = emptyList()
                }
            },
            title = { Text(if (searchResults.isEmpty()) "Add Location" else "Select Location") },
            text = {
                if (searchResults.isEmpty()) {
                    OutlinedTextField(
                        value = cityName,
                        onValueChange = { cityName = it },
                        label = { Text("City or Place Name") },
                        singleLine = true,
                        enabled = !isSearching,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(searchResults) { resultItem ->
                            ListItem(
                                headlineContent = { Text(resultItem.title) },
                                supportingContent = { if (resultItem.subtitle.isNotBlank()) Text(resultItem.subtitle) },
                                modifier = Modifier.clickable {
                                    val newLoc = SavedLocation(
                                        id = UUID.randomUUID().toString(),
                                        name = resultItem.title,
                                        latitude = resultItem.latitude,
                                        longitude = resultItem.longitude
                                    )
                                    viewModel.saveLocation(newLoc)
                                    showAddDialog = false
                                    searchResults = emptyList()
                                    cityName = ""
                                    Toast.makeText(context, "Added ${newLoc.name}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (searchResults.isEmpty()) {
                    TextButton(
                        enabled = cityName.isNotBlank() && !isSearching,
                        onClick = {
                            isSearching = true
                            coroutineScope.launch {
                                try {
                                    val results = withContext(Dispatchers.IO) {
                                        val list = mutableListOf<SearchResultItem>()
                                        
                                        // 1. Try Geocoder first
                                        try {
                                            val geocoder = Geocoder(context)
                                            @Suppress("DEPRECATION")
                                            val addresses = geocoder.getFromLocationName(cityName, 10)
                                            if (addresses != null) {
                                                for (address in addresses) {
                                                    val title = address.locality ?: address.featureName ?: cityName
                                                    val subtitleParts = listOfNotNull(
                                                        address.subAdminArea,
                                                        address.adminArea,
                                                        address.countryName
                                                    ).filter { it.isNotBlank() && it != title }.distinct()
                                                    val subtitle = subtitleParts.joinToString(", ")
                                                    list.add(SearchResultItem(title, subtitle, address.latitude, address.longitude))
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Ignore geocoder errors and fallback to Nominatim
                                        }
                                        
                                        // 2. If geocoder returns <= 1 result, augment with Nominatim
                                        if (list.size <= 1) {
                                            try {
                                                val encodedQuery = java.net.URLEncoder.encode(cityName, "UTF-8")
                                                val url = java.net.URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=10&accept-language=en")
                                                val connection = url.openConnection() as java.net.HttpURLConnection
                                                connection.requestMethod = "GET"
                                                connection.setRequestProperty("User-Agent", "PrayerTimesApp/1.0")
                                                
                                                if (connection.responseCode == 200) {
                                                    val response = connection.inputStream.bufferedReader().readText()
                                                    val jsonArray = org.json.JSONArray(response)
                                                    for (i in 0 until jsonArray.length()) {
                                                        val item = jsonArray.getJSONObject(i)
                                                        val displayName = item.getString("display_name")
                                                        val lat = item.getString("lat").toDouble()
                                                        val lon = item.getString("lon").toDouble()
                                                        
                                                        val parts = displayName.split(", ").map { it.trim() }
                                                        val title = parts.firstOrNull() ?: cityName
                                                        val subtitle = parts.drop(1).joinToString(", ")
                                                        
                                                        // Only add if it's not already in the list (fuzzy match lat/lon)
                                                        val exists = list.any { 
                                                            Math.abs(it.latitude - lat) < 0.1 && Math.abs(it.longitude - lon) < 0.1 
                                                        }
                                                        if (!exists) {
                                                            list.add(SearchResultItem(title, subtitle, lat, lon))
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Ignore Nominatim errors
                                            }
                                        }
                                        list.distinctBy { it.title + it.subtitle }
                                    }
                                    
                                    if (results.isNotEmpty()) {
                                        searchResults = results
                                    } else {
                                        Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error finding location", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSearching = false
                                }
                            }
                        }
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Search")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSearching,
                    onClick = { 
                        if (searchResults.isNotEmpty()) {
                            searchResults = emptyList()
                        } else {
                            showAddDialog = false 
                        }
                    }
                ) {
                    Text(if (searchResults.isNotEmpty()) "Back" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationCard(
    location: SavedLocation,
    onClick: () -> Unit,
    onSetCurrent: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSetCurrent) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = "Set as Current Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
