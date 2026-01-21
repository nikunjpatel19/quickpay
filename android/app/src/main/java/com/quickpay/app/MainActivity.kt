package com.quickpay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickpay.app.presentation.PaymentViewModel
import com.quickpay.app.presentation.RecentPaymentsViewModel
import com.quickpay.app.presentation.ui.CreatePaymentScreen
import com.quickpay.app.presentation.ui.RecentPaymentsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        setContent {
//            Surface(color = MaterialTheme.colorScheme.background) {
//                QuickPayApp()
//            }
//        }
        setContent {
            com.quickpay.app.ui.theme.QuickpayTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    QuickPayApp()
                }
            }
        }
    }
}

private enum class QuickPayScreen { Create, History }

@Composable
private fun QuickPayApp() {
    val paymentVm: PaymentViewModel = viewModel()
    val historyVm: RecentPaymentsViewModel = viewModel()

    var currentScreen by remember { mutableStateOf(QuickPayScreen.Create) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == QuickPayScreen.Create,
                    onClick = { currentScreen = QuickPayScreen.Create },
                    label = { Text("Create") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = currentScreen == QuickPayScreen.History,
                    onClick = { currentScreen = QuickPayScreen.History },
                    label = { Text("History") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                QuickPayScreen.Create -> CreatePaymentScreen(paymentVm)
                QuickPayScreen.History -> RecentPaymentsScreen(historyVm)
            }
        }
    }
}