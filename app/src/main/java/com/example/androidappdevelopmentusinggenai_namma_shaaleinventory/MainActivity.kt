package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModel
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.AssetViewModelFactory
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.screens.*
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui.theme.AndroidAppDevelopmentUsingGenAINammaShaaleInventoryTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AssetViewModel by viewModels {
        AssetViewModelFactory((application as InventoryApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppDevelopmentUsingGenAINammaShaaleInventoryTheme {
                InventoryApp(viewModel)
            }
        }
    }
}

@Composable
fun InventoryApp(viewModel: AssetViewModel) {
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(onLoginSuccess = { role ->
                    viewModel.setUserRole(role)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToList = { navController.navigate("list") },
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToRepairList = { navController.navigate("list?filter=repair") }
                )
            }
            composable("register") { backStackEntry ->
                val capturedPhotoUri by backStackEntry.savedStateHandle
                    .getLiveData<String>("photoUri")
                    .observeAsState()
                
                val scannedBarcode by backStackEntry.savedStateHandle
                    .getLiveData<String>("barcode")
                    .observeAsState()

                RegisterAssetScreen(
                    viewModel = viewModel,
                    onNavigateToCamera = { navController.navigate("camera") },
                    onAssetSaved = { 
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("photoUri")
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("barcode")
                        navController.popBackStack() 
                    },
                    capturedPhotoUri = capturedPhotoUri,
                    scannedBarcode = scannedBarcode
                )
            }
            composable(
                route = "list?filter={filter}",
                arguments = listOf(navArgument("filter") { defaultValue = "all" })
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter")
                AssetListScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { id, name -> 
                        navController.navigate("history/$id/$name") 
                    },
                    onBack = { navController.popBackStack() },
                    initialFilterRepair = filter == "repair"
                )
            }
            composable("report") {
                ReportScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("camera") {
                CameraScreen(
                    onImageCaptured = { uri ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("photoUri", uri.toString())
                        navController.popBackStack()
                    },
                    onBarcodeDetected = { barcode ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("barcode", barcode)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "history/{assetId}/{assetName}",
                arguments = listOf(
                    navArgument("assetId") { type = NavType.IntType },
                    navArgument("assetName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getInt("assetId") ?: 0
                val assetName = backStackEntry.arguments?.getString("assetName") ?: ""
                AssetHistoryScreen(
                    assetId = assetId,
                    assetName = assetName,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
