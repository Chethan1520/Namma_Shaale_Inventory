package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.util.PdfGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val total by viewModel.totalAssetsCount.collectAsState()
    val repair by viewModel.repairAssetsCount.collectAsState()
    val selectedInstitution by viewModel.selectedInstitution.collectAsState()
    
    val institutionName = selectedInstitution?.name ?: "All Institutions"
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isGeneratingPdf by remember { mutableStateOf(false) }

    val reportText = remember(assets, total, repair, institutionName) {
        val broken = assets.count { it.condition == "Broken" }
        val working = assets.count { it.condition == "Working" }
        """
        AUDIT REPORT: $institutionName
        Generated: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date())}
        
        STATISTICS:
        - Total Registered Assets: $total
        - Functional Units: $working
        - Maintenance Required: $repair
        - Beyond Repair: $broken
        
        INVENTORY LOG:
        ${assets.joinToString("\n") { "[${it.condition}] ${it.name} - ${it.serialNumber}" }}
        
        End of Report.
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Visual Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Current Status", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$total Total Assets Managed", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            // Category Breakdown Section
            Text(text = "Inventory Health Chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            val working = assets.count { it.condition == "Working" }
            val needsRepair = assets.count { it.condition == "Needs Repair" }
            val broken = assets.count { it.condition == "Broken" }
            
            if (total > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val sweepWorking = (working.toFloat() / total) * 360f
                                val sweepRepair = (needsRepair.toFloat() / total) * 360f
                                val sweepBroken = (broken.toFloat() / total) * 360f
                                
                                drawArc(
                                    color = Color(0xFF4CAF50),
                                    startAngle = -90f,
                                    sweepAngle = sweepWorking,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFFFF9800),
                                    startAngle = -90f + sweepWorking,
                                    sweepAngle = sweepRepair,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFFF44336),
                                    startAngle = -90f + sweepWorking + sweepRepair,
                                    sweepAngle = sweepBroken,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${((working.toFloat()/total)*100).toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                Text(text = "Healthy", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem("Working", Color(0xFF4CAF50))
                            LegendItem("Repair", Color(0xFFFF9800))
                            LegendItem("Broken", Color(0xFFF44336))
                        }
                    }
                }
            }

            Text(text = "Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val categories = assets.groupBy { it.category }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (category, list) ->
                    val brokenInCategory = list.count { it.condition == "Broken" }
                    val repairInCategory = list.count { it.condition == "Needs Repair" }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = category, fontWeight = FontWeight.Bold)
                                Text(text = "${list.size} items", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (repairInCategory > 0) {
                                    Badge(containerColor = Color(0xFFFF9800)) { Text("$repairInCategory Repair", color = Color.White) }
                                }
                                if (brokenInCategory > 0) {
                                    Badge(containerColor = Color(0xFFF44336)) { Text("$brokenInCategory Broken", color = Color.White) }
                                }
                                if (repairInCategory == 0 && brokenInCategory == 0) {
                                    Badge(containerColor = Color(0xFF4CAF50)) { Text("Healthy", color = Color.White) }
                                }
                            }
                        }
                    }
                }
            }

            // PDF Action Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Text("Export Official Report", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Generate a professional PDF document for your external audit or principal's review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                val pdfFile = withContext(Dispatchers.IO) {
                                    PdfGenerator.generateInventoryPdf(context, assets, institutionName)
                                }
                                isGeneratingPdf = false
                                
                                if (pdfFile != null) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pdfFile
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingPdf && assets.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate & Share PDF Report")
                        }
                    }
                }
            }

            // Paper-style Report Card
            Text(text = "Official Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFEFE))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("INVENTORY AUDIT SHEET", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                    Text(
                        text = reportText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        color = Color(0xFF212121)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}


