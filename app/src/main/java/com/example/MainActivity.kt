package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpenseRepository
import com.example.ui.screens.*
import com.example.ui.theme.SmartExpenseTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.ExpenseViewModelFactory

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ANALYTICS("Insights", Icons.Default.PieChart),
    SCAN_RECEIPT("Scan", Icons.Default.DocumentScanner),
    BANK_SYNC("Bank Sync", Icons.Default.AccountBalance),
    DEV_GUIDE("CS Guide", Icons.Default.School)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(database)
        val viewModelFactory = ExpenseViewModelFactory(repository)

        setContent {
            SmartExpenseTheme {
                val expenseViewModel: ExpenseViewModel = viewModel(factory = viewModelFactory)
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
                var isAddExpenseDialogOpen by remember { mutableStateOf(false) }
                val activeCurrency by expenseViewModel.activeCurrency.collectAsState()

                // BackHandler returns user to Dashboard if on a sub-screen
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    currentScreen = AppScreen.DASHBOARD
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_bottom_nav_bar")
                        ) {
                            AppScreen.entries.forEach { screen ->
                                val selected = currentScreen == screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (currentScreen) {
                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = expenseViewModel,
                                onNavigateToScanReceipt = { currentScreen = AppScreen.SCAN_RECEIPT },
                                onNavigateToBankSync = { currentScreen = AppScreen.BANK_SYNC },
                                onOpenAddExpenseDialog = { isAddExpenseDialogOpen = true },
                                modifier = modifier
                            )
                        }
                        AppScreen.ANALYTICS -> {
                            AnalyticsScreen(
                                viewModel = expenseViewModel,
                                modifier = modifier
                            )
                        }
                        AppScreen.SCAN_RECEIPT -> {
                            ReceiptScannerScreen(
                                viewModel = expenseViewModel,
                                onReceiptSaved = { currentScreen = AppScreen.DASHBOARD },
                                modifier = modifier
                            )
                        }
                        AppScreen.BANK_SYNC -> {
                            BankSyncScreen(
                                viewModel = expenseViewModel,
                                modifier = modifier
                            )
                        }
                        AppScreen.DEV_GUIDE -> {
                            DevArchitectureScreen(
                                modifier = modifier
                            )
                        }
                    }

                    if (isAddExpenseDialogOpen) {
                        AddExpenseDialog(
                            initialCurrency = activeCurrency,
                            onDismiss = { isAddExpenseDialogOpen = false },
                            onSaveExpense = { title, amount, currency, category, notes, merchant ->
                                expenseViewModel.addExpense(title, amount, currency, category, notes, merchant)
                                isAddExpenseDialogOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}
