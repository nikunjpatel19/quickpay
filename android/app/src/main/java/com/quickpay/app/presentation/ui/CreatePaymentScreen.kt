package com.quickpay.app.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quickpay.app.presentation.PaymentViewModel

@Composable
fun CreatePaymentScreen(vm: PaymentViewModel) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    // open Custom Tab when checkoutUrl appears
    LaunchedEffect(state.checkoutUrl) {
        val url = state.checkoutUrl ?: return@LaunchedEffect
        val cti = CustomTabsIntent.Builder().build()
        cti.launchUrl(ctx, Uri.parse(url))
    }

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
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = vm::onDescChanged,
            label = { Text("Description") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.currency,
            onValueChange = vm::onCurrencyChanged,
            label = { Text("Currency (USD/CAD)") },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.createLink() },
            enabled = !state.isLoading
        ) { Text(if (state.isLoading) "Creating..." else "Create Link") }

        Spacer(Modifier.height(24.dp))
        if (state.orderId != null) {
            Text("Order: ${state.orderId}")
            Text("Status: ${state.orderStatus ?: "-"}")
            Text("Checkout: ${state.checkoutUrl ?: "-"}")
        }

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = vm::clearError) { Text("Dismiss") }
        }
    }
}