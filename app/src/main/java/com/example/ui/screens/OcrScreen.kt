package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val extractedText by viewModel.ocrState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                viewModel.performOcr(context, uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
                viewModel.performOcr(context, tempCameraUri!!)
            }
        }
    )

    fun createImageUri(context: Context): Uri {
        val imagePath = File(context.cacheDir, "images")
        imagePath.mkdirs()
        val file = File(imagePath, "camera_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearOcr() }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose to take a photo or select from gallery.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    tempCameraUri = createImageUri(context)
                    cameraLauncher.launch(tempCameraUri!!)
                }) { Text("Camera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) { Text("Gallery") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image to Text (OCR)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Capture or Upload Image", style = MaterialTheme.typography.titleMedium)
            }

            if (selectedImageUri != null && extractedText.isEmpty()) {
                Text("Image selected. Extracting text...", color = MaterialTheme.colorScheme.secondary)
                CircularProgressIndicator()
            }

            if (extractedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Extracted Text:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(extractedText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { clipboardManager.setText(AnnotatedString(extractedText)) }) {
                        Text("Copy Text")
                    }
                    Button(onClick = { viewModel.speak(extractedText, "en") }) {
                        Text("Read Aloud")
                    }
                }
            }
        }
    }
}
