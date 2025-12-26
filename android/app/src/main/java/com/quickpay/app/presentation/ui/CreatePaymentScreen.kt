package com.quickpay.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quickpay.app.presentation.PaymentViewModel

@Composable
fun CreatePaymentScreen(vm: PaymentViewModel) {
    val state by vm.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Create Payment", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.amountText,
            onValueChange = vm::onAmountChanged,
            label = { Text("Amount (cents)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = vm::onDescChanged,
            label = { Text("Description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.currency,
            onValueChange = vm::onCurrencyChanged,
            label = { Text("Currency (USD/CAD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.createLink() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Creating..." else "Create Link")
        }

        Spacer(Modifier.height(24.dp))

        // Show order info and QR once link is created
        if (state.orderId != null) {
            Text("Order: ${state.orderId}")
            Text("Status: ${state.orderStatus ?: "-"}")
        }

        val checkoutUrl = state.checkoutUrl

        if (checkoutUrl != null) {
            Spacer(Modifier.height(16.dp))

            Text(
                "Show this QR to your customer",
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                QrCode(
                    data = checkoutUrl,
                    size = 220.dp
                )
            }
        }

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = vm::clearError) { Text("Dismiss") }
        }
    }
}