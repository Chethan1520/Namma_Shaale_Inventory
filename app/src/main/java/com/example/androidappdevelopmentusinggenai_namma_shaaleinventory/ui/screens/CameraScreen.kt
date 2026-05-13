package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreviewContent(onImageCaptured, onBarcodeDetected, onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required.")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreviewContent(
    onImageCaptured: (Uri) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val scanner = remember { BarcodeScanning.getClient() }
    
    var isDetected by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    if (isDetected) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty() && !isDetected) {
                                    val barcode = barcodes[0].rawValue
                                    if (barcode != null) {
                                        isDetected = true
                                        onBarcodeDetected(barcode)
                                    }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
            }
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalyzer
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    LaunchedEffect(flashEnabled) {
        camera?.cameraControl?.enableTorch(flashEnabled)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            color = Color.Black.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    "Asset Documentation",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { flashEnabled = !flashEnabled }) {
                    Icon(
                        if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) Color.Yellow else Color.White
                    )
                }
            }
        }

        // Professional Framing Overlay (Corners only, not a restrictive box)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
        ) {
            // Drawing L-shaped corners for a professional "Viewfinder" look
            val cornerColor = if (isDetected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
            val strokeWidth = 3.dp
            val cornerLength = 40.dp

            // We use simple boxes for corners
            Box(Modifier.size(cornerLength, strokeWidth).background(cornerColor).align(Alignment.TopStart))
            Box(Modifier.size(strokeWidth, cornerLength).background(cornerColor).align(Alignment.TopStart))

            Box(Modifier.size(cornerLength, strokeWidth).background(cornerColor).align(Alignment.TopEnd))
            Box(Modifier.size(strokeWidth, cornerLength).background(cornerColor).align(Alignment.TopEnd))

            Box(Modifier.size(cornerLength, strokeWidth).background(cornerColor).align(Alignment.BottomStart))
            Box(Modifier.size(strokeWidth, cornerLength).background(cornerColor).align(Alignment.BottomStart))

            Box(Modifier.size(cornerLength, strokeWidth).background(cornerColor).align(Alignment.BottomEnd))
            Box(Modifier.size(strokeWidth, cornerLength).background(cornerColor).align(Alignment.BottomEnd))
        }

        // Bottom Controls and Feedback
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isDetected) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        "Barcode Automatically Scanned",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Text(
                    "Point at asset to scan ID or capture photo",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                "Take a clear, full photo of the equipment",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            IconButton(
                onClick = { takePhoto(context, imageCapture, onImageCaptured) },
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Capture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCaptured: (Uri) -> Unit) {
    // Use filesDir for permanent storage, not cacheDir
    val imageFolder = File(context.filesDir, "asset_images")
    if (!imageFolder.exists()) imageFolder.mkdirs()
    
    val photoFile = File(imageFolder, "IMG_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                // Return the persistent URI
                onImageCaptured(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) { exception.printStackTrace() }
        }
    )
}
