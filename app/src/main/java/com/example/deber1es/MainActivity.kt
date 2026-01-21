package com.example.deber1es

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.example.deber1es.ui.theme.Deber1ESTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Deber1ESTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        AccidentFormScreen()
                    }
                }
            }
        }
    }
}

data class AccidentRecord(
    val type: String,
    val date: String,
    val plate: String,
    val driverName: String,
    val driverId: String,
    val notes: String,
    val photoUri: Uri?,
    val locationText: String?
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccidentFormScreen() {
    val context = LocalContext.current
    val vibrator = context.getSystemService<Vibrator>()

    // Estado del formulario
    var accidentType by remember { mutableStateOf("Choque") }
    val accidentTypes = listOf("Choque", "Colisión", "Atropello")
    var plate by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Foto
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Ubicación
    var locationText by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val accidents = remember { mutableStateListOf<AccidentRecord>() }

    // Launchers para abrir activities separadas
    val cameraActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uriString = result.data?.getStringExtra(CameraActivity.EXTRA_PHOTO_URI)
            val uri = uriString?.let(Uri::parse)
            photoUri = uri
            capturedBitmap = uri?.loadBitmap(context.contentResolver)
        }
    }

    val locationActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationText = result.data?.getStringExtra(LocationActivity.EXTRA_LOCATION_TEXT)
        }
    }

    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )

    fun vibratePhone() {
        vibrator?.let { vib ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(5_000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(5_000)
            }
        }
    }

    fun onSaveAccident() {
        accidents.add(
            AccidentRecord(
                type = accidentType,
                date = dateFormatter.format(Date(selectedDate)),
                plate = plate.trim(),
                driverName = driverName.trim(),
                driverId = driverId.trim(),
                notes = notes.trim(),
                photoUri = photoUri,
                locationText = locationText
            )
        )
        vibratePhone()
        Toast.makeText(context, "Accidente guardado", Toast.LENGTH_SHORT).show()
        
        // Limpiar todos los campos del formulario
        accidentType = "Choque"
        plate = ""
        driverName = ""
        driverId = ""
        notes = ""
        selectedDate = Calendar.getInstance().timeInMillis
        photoUri = null
        capturedBitmap = null
        locationText = null
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Registro de accidentes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Agente de tránsito",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de accidente
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = accidentType,
                    onValueChange = { },
                    label = { Text("Tipo de accidente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accidentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                accidentType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fecha del siniestro
            OutlinedTextField(
                value = dateFormatter.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha del siniestro") },
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Cambiar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    selectedDate = millis
                                }
                                showDatePicker = false
                            }
                        ) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text("Matrícula del auto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = driverName,
                onValueChange = { driverName = it },
                label = { Text("Nombre del conductor") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = driverId,
                onValueChange = { driverId = it },
                label = { Text("Cédula del conductor") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observaciones") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Captura de fotos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fotografía del siniestro", fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = {
                                cameraActivityLauncher.launch(
                                    Intent(context, CameraActivity::class.java)
                                )
                            }
                        ) {
                            Text("Abrir cámara")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        capturedBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Text("Sin foto aún")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ubicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ubicación GPS", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            locationActivityLauncher.launch(
                                Intent(context, LocationActivity::class.java)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Abrir ubicación"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = locationText ?: "Ubicación pendiente",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSaveAccident() },
                enabled = plate.isNotBlank() && driverName.isNotBlank() && driverId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar accidente (vibrar 5s)")
            }

            if (accidents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Registros recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                accidents.forEach { record ->
                    AccidentSummary(record)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AccidentSummary(record: AccidentRecord) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val bitmap = remember(record.photoUri) {
        record.photoUri?.loadBitmap(context.contentResolver)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { showDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto miniatura
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Foto del accidente",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Sin foto",
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text("${record.type} - ${record.date}", fontWeight = FontWeight.SemiBold)
                Text("Placa: ${record.plate}", style = MaterialTheme.typography.bodyMedium)
                Text("Conductor: ${record.driverName}", style = MaterialTheme.typography.bodySmall)
                record.locationText?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Diálogo para ver detalles completos
    if (showDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Detalles del accidente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Foto completa
                    bitmap?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Foto del accidente",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Información del accidente
                    DetailRow(label = "Tipo de accidente", value = record.type)
                    DetailRow(label = "Fecha", value = record.date)
                    DetailRow(label = "Matrícula", value = record.plate)
                    DetailRow(label = "Conductor", value = record.driverName)
                    DetailRow(label = "Cédula", value = record.driverId)
                    
                    record.locationText?.let {
                        DetailRow(label = "Ubicación GPS", value = it)
                    }
                    
                    if (record.notes.isNotBlank()) {
                        DetailRow(label = "Observaciones", value = record.notes)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun Uri.loadBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        contentResolver.openInputStream(this)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccidentPreview() {
    Deber1ESTheme {
        AccidentFormScreen()
    }
}