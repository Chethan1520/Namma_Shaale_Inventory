package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data.Asset
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    viewModel: AssetViewModel,
    onNavigateToHistory: (Int, String) -> Unit,
    onBack: () -> Unit,
    initialFilterRepair: Boolean = false
) {
    val assets by viewModel.allAssets.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedAssetForNote by remember { mutableStateOf<Asset?>(null) }
    var pendingCondition by remember { mutableStateOf("") }
    var showOnlyRepairRequired by remember { mutableStateOf(initialFilterRepair) }

    val filteredAssets = assets.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || 
                          it.serialNumber.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (showOnlyRepairRequired) it.condition != "Working" else true
        matchesSearch && matchesFilter
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + assets.map { it.category }.distinct()
    
    val finalFilteredAssets = filteredAssets.filter {
        if (selectedCategory == "All") true else it.category == selectedCategory
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    TopAppBar(
                        title = { 
                            Text(
                                if (showOnlyRepairRequired) "Repair Requests" else "Inventory Audit",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showOnlyRepairRequired = !showOnlyRepairRequired }) {
                                Icon(
                                    if (showOnlyRepairRequired) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    // Search Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search assets or serial numbers...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }

                    // Category Filter Chips
                    if (categories.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.Transparent,
                                        selectedContainerColor = Color.White,
                                        labelColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.White.copy(alpha = 0.5f),
                                        selectedBorderColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (finalFilteredAssets.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No assets found", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(finalFilteredAssets, key = { it.id }) { asset ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    AssetItem(
                        asset = asset,
                        onUpdateCondition = { newCond ->
                            if (newCond == "Working") {
                                viewModel.updateAssetCondition(asset, newCond, null)
                            } else {
                                pendingCondition = newCond
                                selectedAssetForNote = asset
                            }
                        },
                        onViewHistory = { onNavigateToHistory(asset.id, asset.name) },
                        onDelete = { showDeleteDialog = true }
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Asset?") },
                            text = { Text("Are you sure you want to remove ${asset.name} from the inventory? This will also delete its history.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteAsset(asset)
                                        showDeleteDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            // Issue Log Dialog
            selectedAssetForNote?.let { asset ->
                var noteText by remember { mutableStateOf(asset.note ?: "") }
                AlertDialog(
                    onDismissRequest = { selectedAssetForNote = null },
                    title = { Text("Log Issue: ${asset.name}") },
                    text = {
                        Column {
                            Text("Marking as: $pendingCondition", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Reason for health status") },
                                placeholder = { Text("e.g., Cracked screen, lost during sports meet") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.updateAssetCondition(asset, pendingCondition, noteText)
                            selectedAssetForNote = null
                        }) {
                            Text("Update Status")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedAssetForNote = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AssetItem(
    asset: Asset,
    onUpdateCondition: (String) -> Unit,
    onViewHistory: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${asset.category} • S/N: ${asset.serialNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                ConditionBadge(condition = asset.condition)
            }

            // Photo Preview (if exists)
            if (!asset.photoUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = asset.photoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // History Icon Overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clickable { onViewHistory() },
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History",
                            modifier = Modifier.padding(8.dp),
                            tint = Color.White
                        )
                    }
                }
            } else {
                // If no photo, show history button normally
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                   IconButton(onClick = onViewHistory) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Status and Notes
            Column(modifier = Modifier.padding(16.dp)) {
                if (!asset.note.isNullOrEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = asset.note,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = Color.Red
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = "Last Audit: ${dateFormat.format(Date(asset.lastChecked))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                Text(
                    text = "Quick Update Status:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UpdateChip(label = "Working", isSelected = asset.condition == "Working", color = Color(0xFF4CAF50)) { onUpdateCondition("Working") }
                    UpdateChip(label = "Needs Repair", isSelected = asset.condition == "Needs Repair", color = Color(0xFFFF9800)) { onUpdateCondition("Needs Repair") }
                    UpdateChip(label = "Broken", isSelected = asset.condition == "Broken", color = Color(0xFFF44336)) { onUpdateCondition("Broken") }
                    
                    Spacer(Modifier.weight(1f))
                    
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp)),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) color else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ConditionBadge(condition: String) {
    val (color, icon) = when (condition) {
        "Working" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        "Needs Repair" -> Color(0xFFFF9800) to Icons.Default.Error
        "Broken" -> Color(0xFFF44336) to Icons.Default.Cancel
        else -> Color.Gray to Icons.Default.Help
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            Text(
                text = condition,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

