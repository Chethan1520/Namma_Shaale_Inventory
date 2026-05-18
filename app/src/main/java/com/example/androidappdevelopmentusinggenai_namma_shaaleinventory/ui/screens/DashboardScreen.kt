package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AssetViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToRepairList: () -> Unit,
    onLogout: () -> Unit
) {
    val totalAssets by viewModel.totalAssetsCount.collectAsState()
    val needsRepair by viewModel.repairAssetsCount.collectAsState()
    val institutions by viewModel.allInstitutions.collectAsState()
    val selectedInstitution by viewModel.selectedInstitution.collectAsState()
    
    var showInstitutionDialog by remember { mutableStateOf(false) }
    var newInstName by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F172A), // Modern Dark Background
        topBar = {
            TopAppBar(
                title = { Text("Inventory Auditor", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f)) },
                actions = {
                    TextButton(onClick = { 
                        viewModel.logout()
                        onLogout() 
                    }) {
                        Text("Logout", color = Color(0xFFF87171))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (selectedInstitution == null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Institution") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8), // Light Blue
                                Color(0xFF0284C7)  // Deep Blue
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val greeting = remember {
                            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            when (hour) {
                                in 0..11 -> "Good Morning"
                                in 12..16 -> "Good Afternoon"
                                else -> "Good Evening"
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedInstitution?.name ?: "Namma-Shaale",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = if (selectedInstitution != null) "Active Audit Session" else greeting,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { showInstitutionDialog = true }) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Switch Institution",
                                    modifier = Modifier.padding(10.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickStatCard(
                            label = "Total Assets",
                            count = totalAssets.toString(),
                            icon = Icons.Default.Inventory2,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            label = "Needs Repair",
                            count = needsRepair.toString(),
                            icon = Icons.Default.Build,
                            contentColor = Color(0xFFF87171),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (selectedInstitution == null && totalAssets == 0 && institutions.isEmpty()) {
                // Onboarding / Selection View
                Column(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("Welcome Auditor!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Select or register an institution to start.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { showInstitutionDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Text("Select Institution")
                    }
                }
            } else {
                // Main Menu Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Inventory Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MenuCard(
                            title = "Register Asset",
                            subtitle = "Add new items",
                            icon = Icons.Default.Add,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            enabled = selectedInstitution != null,
                            onClick = onNavigateToRegister
                        )
                        MenuCard(
                            title = "View Inventory",
                            subtitle = "Audit & Health",
                            icon = Icons.AutoMirrored.Filled.List,
                            color = Color(0xFF818CF8),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToList
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActionTile(
                            title = "Repair List",
                            icon = Icons.Default.Handyman,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRepairList
                        )
                        ActionTile(
                            title = "Insights",
                            icon = Icons.Default.Assessment,
                            color = Color(0xFFA855F7),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReport
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Priority Repairs Section
                    Text(
                        text = "Priority Repairs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val assets by viewModel.allAssets.collectAsState()
                    val criticalItems = assets.filter { it.condition != "Working" }.take(3)

                    if (criticalItems.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80))
                                Spacer(Modifier.width(12.dp))
                                Text("All systems operational.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            criticalItems.forEach { item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(CircleShape).background(if (item.condition == "Broken") Color(0xFFF87171) else Color(0xFFF59E0B))
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(item.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        ConditionBadge(condition = item.condition)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // Institution Selection Dialog
        if (showInstitutionDialog) {
            AlertDialog(
                onDismissRequest = { showInstitutionDialog = false },
                title = { Text("Audit Session") },
                text = {
                    Column {
                        Text("Choose an institution:", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        item {
                            ListItem(
                                headlineContent = { Text("All Schools (Full Audit)", fontWeight = FontWeight.Bold) },
                                leadingContent = { Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF38BDF8)) },
                                trailingContent = {
                                    if (selectedInstitution == null) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectInstitution(-1)
                                    showInstitutionDialog = false
                                }
                            )
                        }
                        items(institutions) { inst ->
                                ListItem(
                                    headlineContent = { Text(inst.name) },
                                    leadingContent = { Icon(Icons.Default.School, contentDescription = null) },
                                    trailingContent = {
                                        Row {
                                            if (inst.id == selectedInstitution?.id) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteInstitution(inst) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.selectInstitution(inst.id)
                                        showInstitutionDialog = false
                                    }
                                )
                            }
                        }
                        TextButton(onClick = { showAddDialog = true; showInstitutionDialog = false }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Register New Institution")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInstitutionDialog = false }) { Text("Close") }
                }
            )
        }

        // Add Institution Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Register Institution") },
                text = {
                    OutlinedTextField(
                        value = newInstName,
                        onValueChange = { newInstName = it },
                        label = { Text("Institution Name") },
                        placeholder = { Text("e.g., BMS School") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newInstName.isNotBlank()) {
                            viewModel.addInstitution(newInstName)
                            newInstName = ""
                            showAddDialog = false
                        }
                    }) { Text("Register") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}


@Composable
fun QuickStatCard(
    label: String,
    count: String,
    icon: ImageVector,
    contentColor: Color = Color(0xFF38BDF8),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.6f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = count,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (enabled) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (enabled) color.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = if (enabled) color else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (enabled) Color.White else Color.Gray
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }
    }
}
