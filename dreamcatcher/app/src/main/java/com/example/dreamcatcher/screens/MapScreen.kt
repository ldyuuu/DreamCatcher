package com.example.dreamcatcher.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.R
import com.example.dreamcatcher.models.Location
import com.example.dreamcatcher.models.TherapyCenter
import com.example.dreamcatcher.network.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import com.google.maps.android.compose.CameraPositionState


@Composable
fun MapScreen(
    apiKey: String,
    viewModel: MainViewModel,
    navController: NavController
) {
    val cameraPositionState = rememberCameraPositionState()
    val therapyCenters by viewModel.therapyCenters.observeAsState(emptyList())
    val userAddress = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val userLatLng = remember { mutableStateOf<LatLng?>(null) }
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val email = loggedInUser?.email

    LaunchedEffect(email) {
        email?.let {
            val user = viewModel.getUserByEmailSync(it)
            userAddress.value = user?.address
            Log.e("MapScreen", "User: ${user?.userId} User Address: ${user?.address}")

            user?.address?.let { address ->
                viewModel.fetchUserLocation(address, apiKey) { location ->
                    location?.let {
                        userLatLng.value = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            12f
                        )
                    } ?: Log.e("MapScreen", "Failed to fetch user location")
                }
            }

            viewModel.fetchTherapyCenters(email, apiKey)
        }
    }

    LaunchedEffect(therapyCenters) {
        if (therapyCenters.isNotEmpty()) {
            val firstCenter = therapyCenters.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(firstCenter.latitude, firstCenter.longitude),
                12f
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Map Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                therapyCenters.forEach { center ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(center.latitude, center.longitude)
                        ),
                        title = center.name,
                        snippet = center.address,
                        onClick = {
                            Log.d("MapScreen", "Clicked on: ${center.name}")
                            true
                        }
                    )
                }

                userLatLng.value?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "You are here",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                SearchNearbyButton(
                    cameraPositionState = cameraPositionState,
                    viewModel = viewModel,
                    apiKey = apiKey
                )
            }
        }

        // Therapy Center Details Section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            items(therapyCenters) { center ->
                TherapyCenterRow(center = center, apiKey = apiKey) {
                    Log.d("TherapyCenterRow", "Clicked on: ${center.name}")
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                LatLng(center.latitude, center.longitude),
                                20f
                            ),
                            durationMs = 500
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TherapyCenterRow(center: TherapyCenter, apiKey: String, onClick: () -> Unit) {
    val photoUrl = center.photoReference?.let {
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$it&key=$apiKey"
    } ?: "https://via.placeholder.com/100"

    Log.d("TherapyCenterRow", "Photo Reference: ${center.photoReference}")
    Log.d("TherapyCenterRow", "Photo URL: $photoUrl")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                Log.d("TherapyCenterRow", "Row clicked: ${center.name}")
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Image Section
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUrl),
                contentDescription = "Therapy Center Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = center.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = center.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        OpenMapButton(center = center)

    }
}


private suspend fun findNearbyTherapies(
    lat: Double,
    lng: Double,
    apiKey: String
): List<TherapyCenter> {
    return try {
        val location = "$lat,$lng"
        val response = RetrofitInstance.placesAPI.findPlaces(
            location,
            radius = 5000,
            type = "health",
            keyword = "therapist|psychiatrist",
            apiKey
        )

        Log.d("PlacesAPI Response", "Raw response: $response")

        if (response.status == "OK" && response.results.isNotEmpty()) {
            response.results.map { place ->
                Log.d("PlacesAPI Result", "Place: $place")

                TherapyCenter(
                    name = place.name,
                    address = place.vicinity,
                    latitude = place.geometry.location.lat,
                    longitude = place.geometry.location.lng,
                    photoReference = place.photos?.firstOrNull()?.photoReference
                )
            }
        } else {
            Log.e("PlacesAPI Error", "Failed to find places: ${response.status}")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("PlacesAPI Exception", "Error: ${e.message}")
        emptyList()
    }
}


fun MainViewModel.fetchTherapyCentersByLocation(
    latitude: Double,
    longitude: Double,
    apiKey: String
) {
    viewModelScope.launch {
        val centers = findNearbyTherapies(latitude, longitude, apiKey)
        therapyCenters.postValue(centers)
    }
}


@Composable
fun OpenMapButton(center: TherapyCenter) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable {
                //geo:latitude,longitude?q=location_name for maps
                val uri = "geo:${center.latitude},${center.longitude}?q=${center.name}"
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                mapIntent.resolveActivity(context.packageManager)
                    ?.let {
                        context.startActivity(mapIntent)
                    } ?: Log.e("TherapyCenterRow", "No map application found")
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.navigation),
            contentDescription = "Open in Map",
            tint = Color.Unspecified,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "Open in Map",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SearchNearbyButton(
    cameraPositionState: CameraPositionState,
    viewModel: MainViewModel,
    apiKey: String
) {
    val isLoading = remember { mutableStateOf(false) }
    val therapyCenters by viewModel.therapyCenters.observeAsState(emptyList())

    LaunchedEffect(therapyCenters) {
        if (isLoading.value) {
            isLoading.value = false
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .clickable(enabled = !isLoading.value) {
                isLoading.value = true
                val currentLatLng = cameraPositionState.position.target
                viewModel.fetchTherapyCentersByLocation(
                    latitude = currentLatLng.latitude,
                    longitude = currentLatLng.longitude,
                    apiKey = apiKey
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black
            )
        } else {
            Text(
                text = "Search Nearby",
                color = Color.Black
            )
        }
    }
}

suspend fun geocodeAddress(address: String, apiKey: String): Location? {
    return try {
        val response = RetrofitInstance.geocodingAPI.geocode(address, apiKey)
        val locationData = response.results.firstOrNull()?.geometry?.location

        locationData?.let {
            Location(lat = it.lat, lng = it.lng)
        }
    } catch (e: Exception) {
        Log.e("GeocodeAddress", "Error geocoding address: ${e.message}")
        null
    }
}
