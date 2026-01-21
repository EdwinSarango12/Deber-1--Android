package com.example.deber1es

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.deber1es.ui.theme.Deber1ESTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Deber1ESTheme {
                Scaffold { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        LocationScreen(
                            onDone = { locationText ->
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(EXTRA_LOCATION_TEXT, locationText)
                                )
                                finish()
                            },
                            onCancel = {
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_LOCATION_TEXT = "locationText"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationScreen(
    onDone: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var locationText by remember { mutableStateOf<String?>(null) }
    var isRequestingLocation by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    locationText =
                        "Lat: ${location.latitude}, Lon: ${location.longitude}, Precisión: ${location.accuracy}m"
                }
                isRequestingLocation = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
            return
        }
        isRequestingLocation = true
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .setMaxUpdates(1)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            context.mainLooper
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ubicación GPS",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(locationText ?: "Ubicación pendiente")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { requestLocation() },
                enabled = !isRequestingLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isRequestingLocation -> "Obteniendo ubicación..."
                        locationPermissions.allPermissionsGranted -> "Actualizar ubicación"
                        else -> "Solicitar permisos"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { locationText?.let(onDone) },
                enabled = locationText != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar esta ubicación")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

