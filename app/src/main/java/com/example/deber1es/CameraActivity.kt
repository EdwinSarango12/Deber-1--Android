package com.example.deber1es

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.deber1es.ui.theme.Deber1ESTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Deber1ESTheme {
                Scaffold { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        CameraScreen(
                            onDone = { uri ->
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(EXTRA_PHOTO_URI, uri?.toString())
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
        const val EXTRA_PHOTO_URI = "photoUri"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraScreen(
    onDone: (Uri?) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                capturedBitmap = uri.loadBitmap(context.contentResolver)
            }
        }
    }

    fun createImageUri(): Uri? {
        val timeStamp = System.currentTimeMillis()
        val imageFile = File.createTempFile(
            "ACCIDENT_$timeStamp",
            ".jpg",
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
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
                text = "Cámara",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Aún no has tomado una foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (cameraPermission.status.isGranted) {
                        createImageUri()?.let { uri ->
                            photoUri = uri
                            takePictureLauncher.launch(uri)
                        }
                    } else {
                        cameraPermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cameraPermission.status.isGranted) "Tomar foto" else "Solicitar permiso de cámara")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onDone(photoUri) },
                enabled = photoUri != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar esta foto")
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

private fun Uri.loadBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        contentResolver.openInputStream(this)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (_: Exception) {
        null
    }
}

