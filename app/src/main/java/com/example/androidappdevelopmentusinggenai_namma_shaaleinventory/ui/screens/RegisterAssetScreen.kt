package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModel
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAssetScreen(
    viewModel: AssetViewModel,
    onNavigateToCamera: () -> Unit,
    onAssetSaved: () -> Unit,
    capturedPhotoUri: String? = null,
    scannedBarcode: String? = null
) {
    val institutions by viewModel.allInstitutions.collectAsState()
    val selectedInstitution by viewModel.selectedInstitution.collectAsState()

    var name by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var customCategory by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Working") }
    var priority by remember { mutableStateOf("Medium") }
    var estimatedRepairCost by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    var currentPhotoUri by remember { mutableStateOf(capturedPhotoUri) }
    
    LaunchedEffect(capturedPhotoUri) {
        if (capturedPhotoUri != null) {
            currentPhotoUri = capturedPhotoUri
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            uri?.let { 
                val persistentUri = FileUtil.copyUriToInternalStorage(context, it, "asset_images")
                currentPhotoUri = persistentUri?.toString() ?: it.toString()
            } 
        }
    )

    val barcodeGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            uri?.let { 
                val image = com.google.mlkit.vision.common.InputImage.fromFilePath(context, it)
                val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            serialNumber = barcodes[0].rawValue ?: ""
                        }
                    }
            }
        }
    )
    
    var showInstSelector by remember { mutableStateOf(false) }
    var localSelectedInst by remember { mutableStateOf(selectedInstitution) }

    LaunchedEffect(selectedInstitution) {
        localSelectedInst = selectedInstitution
    }

    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null) {
            serialNumber = scannedBarcode
        }
    }
    
    val categories = listOf("General", "Lab", "Sports", "IT", "Classroom", "Other")
    val conditions = listOf("Working", "Needs Repair", "Broken")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Asset") },
                navigationIcon = {
                    IconButton(onClick = onAssetSaved) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Visual Header for the form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asset Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { showInstSelector = true }) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(localSelectedInst?.name ?: "Select School", fontSize = 12.sp)
                }
            }

            // Photo / Scanner Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentPhotoUri == null) 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentPhotoUri != null) {
                        AsyncImage(
                            model = currentPhotoUri,
                            contentDescription = "Asset Photo",
                            modifier = Modifier.fillMaxSize().clickable { onNavigateToCamera() },
                            contentScale = ContentScale.Crop
                        )
                        // Floating Badge if barcode scanned
                        if (scannedBarcode != null) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Barcode Scanned", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().clickable { onNavigateToCamera() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.padding(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Tap to capture photo", fontWeight = FontWeight.Medium)
                            Text("Auto-scans barcode if visible", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    
                    // Gallery Action Button
                    SmallFloatingActionButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                    }
                }
            }

            // Input Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Asset Name") },
                placeholder = { Text("e.g., Compound Microscope") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Room / Location") },
                placeholder = { Text("e.g., Science Lab, Room 10A") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
            )

            OutlinedTextField(
                value = serialNumber,
                onValueChange = { serialNumber = it },
                label = { Text("Serial Number / Tag ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { 
                    Row {
                        IconButton(onClick = { barcodeGalleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.ImageSearch, contentDescription = "Scan Gallery", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = onNavigateToCamera) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Camera", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )

            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { 
                                category = cat
                                if (cat != "Other") customCategory = ""
                            },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (category == "Other") {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Enter Custom Category / Department") },
                        placeholder = { Text("e.g., Computer Network Lab, DDCO Lab") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                    )
                }
            }

            Column {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Initial Condition",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.forEach { cond ->
                        val isSelected = condition == cond
                        val chipColor = when(cond) {
                            "Working" -> if (isSelected) Color(0xFF4CAF50) else Color.Gray
                            "Needs Repair" -> if (isSelected) Color(0xFFFF9800) else Color.Gray
                            "Broken" -> if (isSelected) Color(0xFFF44336) else Color.Gray
                            else -> Color.Gray
                        }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { condition = cond },
                            label = { Text(cond) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                selectedLabelColor = chipColor
                            )
                        )
                    }
                }
            }

            if (condition != "Working") {
                OutlinedTextField(
                    value = estimatedRepairCost,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) estimatedRepairCost = it },
                    label = { Text("Estimated Repair Cost (Optional) (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) }
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Additional Notes (Optional)") },
                placeholder = { Text("Describe physical state or specific location...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalCategory = if (category == "Other" && customCategory.isNotBlank()) customCategory else category
                        viewModel.addAsset(
                            name = name,
                            serialNumber = serialNumber,
                            category = finalCategory,
                            location = location.ifBlank { "General" },
                            condition = condition,
                            priority = priority,
                            estimatedRepairCost = estimatedRepairCost.toDoubleOrNull(),
                            note = note.ifBlank { null },
                            photoUri = currentPhotoUri,
                            institutionId = localSelectedInst?.id
                        )
                        onAssetSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Register Asset", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (showInstSelector) {
            AlertDialog(
                onDismissRequest = { showInstSelector = false },
                title = { Text("Assign to Institution") },
                text = {
                    Column {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(institutions) { inst ->
                                ListItem(
                                    headlineContent = { Text(inst.name) },
                                    modifier = Modifier.clickable {
                                        localSelectedInst = inst
                                        showInstSelector = false
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInstSelector = false }) { Text("Done") }
                }
            )
        }
    }
}
