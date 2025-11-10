package com.quickpay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import com.quickpay.app.presentation.PaymentViewModel
import com.quickpay.app.presentation.ui.CreatePaymentScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm = remember { PaymentViewModel() }
            Surface(color = MaterialTheme.colorScheme.background) {
                CreatePaymentScreen(vm)
            }
        }
    }
}