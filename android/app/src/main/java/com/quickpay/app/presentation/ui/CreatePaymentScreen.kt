package com.quickpay.app.presentation.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CreatePaymentScreen(vm: PaymentViewModel) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val status = (state.orderStatus ?: "").uppercase()
    val statusLabel = when (status) {
        "CREATED" -> "Awaiting payment"
        "AUTHORIZED" -> "Authorized"
        "CAPTURED" -> "Paid"
        "FAILED" -> "Failed"
        else -> if (status.isBlank()) "-" else status.lowercase()
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
            label = { Text("Amount (e.g., 9.99)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
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
            onClick = vm::createLink,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text("Creating...")
            } else {
                Text("Create Link")
            }
        }

        Spacer(Modifier.height(24.dp))

        if (state.orderId != null) {
            Text("Order: ${state.orderId}")
            Spacer(Modifier.height(6.dp))
            AssistChip(
                onClick = {},
                label = { Text(statusLabel) }
            )
        }

        val checkoutUrl = state.checkoutUrl

        if (!checkoutUrl.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))

            Text("Show this QR to your customer", style = MaterialTheme.typography.titleMedium)

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Open") }

                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(checkoutUrl))
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Copy") }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, checkoutUrl)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share link"))
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Share") }
            }

            Spacer(Modifier.height(8.dp))
            Text(checkoutUrl, style = MaterialTheme.typography.bodySmall)
        }

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = vm::clearError) { Text("Dismiss") }
        }
    }
}