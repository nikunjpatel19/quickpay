package com.quickpay.app.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quickpay.app.presentation.CreateFlowStep
import com.quickpay.app.presentation.Currency
import com.quickpay.app.presentation.PaymentViewModel

@Composable
fun CreatePaymentScreen(vm: PaymentViewModel) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create payment", style = MaterialTheme.typography.headlineSmall)

        when (state.step) {
            CreateFlowStep.INPUT -> {
                PaymentDetailsCard(
                    amount = state.amountText,
                    description = state.description,
                    note = state.note,
                    currency = state.currency,
                    isLoading = state.isLoading,
                    onAmountChanged = vm::onAmountChanged,
                    onDescChanged = vm::onDescChanged,
                    onNoteChanged = vm::onNoteChanged,
                    onCurrencySelected = vm::onCurrencySelected,
                    onCreate = vm::createLink
                )
            }

            CreateFlowStep.PAYMENT -> {
                PaymentLinkCard(
                    orderId = state.orderId,
                    statusRaw = state.orderStatus,
                    checkoutUrl = state.checkoutUrl,
                    onEdit = vm::editDetails,
                    onNew = vm::startNewPayment,
                    onCancel = vm::cancelCurrentOrder,
                    isLoading = state.isLoading
                )
            }
        }

        if (state.error != null) {
            ErrorCard(
                message = state.error!!,
                onDismiss = vm::clearError
            )
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    amount: String,
    description: String,
    note: String,
    currency: Currency,
    isLoading: Boolean,
    onAmountChanged: (String) -> Unit,
    onDescChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onCreate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Payment details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChanged,
                label = { Text("Amount") },
                placeholder = { Text("9.99") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescChanged,
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Note (optional)") },
                placeholder = { Text("Table 7, Receipt #1042, etc.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            CurrencySelector(
                selected = currency,
                onSelected = onCurrencySelected
            )

            Button(
                onClick = onCreate,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isLoading) "Creating..." else "Create link")
            }
        }
    }
}

@Composable
private fun CurrencySelector(
    selected: Currency,
    onSelected: (Currency) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Currency", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selected == Currency.USD,
                onClick = { onSelected(Currency.USD) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("USD") }
            )
            SegmentedButton(
                selected = selected == Currency.CAD,
                onClick = { onSelected(Currency.CAD) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("CAD") }
            )
        }
    }
}

@Composable
private fun PaymentLinkCard(
    orderId: String?,
    statusRaw: String?,
    checkoutUrl: String?,
    onEdit: () -> Unit,
    onNew: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current

    val canCancel = when (statusRaw?.lowercase()) {
        null, "created", "pending" -> true
        "captured", "paid", "failed", "cancelled", "canceled", "deactivated" -> false
        else -> true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Payment link", style = MaterialTheme.typography.titleMedium)

            if (orderId != null) Text("Order: $orderId")

            StatusChip(statusRaw = statusRaw)

            if (checkoutUrl != null) {
                Text("Show this QR to your customer")

                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCode(data = checkoutUrl, size = 220.dp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                            context.startActivity(i)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open")
                    }

                    OutlinedButton(
                        onClick = {
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, checkoutUrl)
                            }
                            context.startActivity(Intent.createChooser(i, "Share payment link"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }
                }

                Text(checkoutUrl, style = MaterialTheme.typography.bodySmall)
            }

            if (canCancel) {
                Button(
                    onClick = onCancel,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Cancelling..." else "Cancel payment link")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit details") }
                TextButton(onClick = onNew, modifier = Modifier.weight(1f)) { Text("New payment") }
            }
        }
    }
}

@Composable
public fun StatusChip(statusRaw: String?) {
    val label = when (statusRaw?.lowercase()) {
        "captured", "paid" -> "Paid"
        "failed" -> "Failed"
        "created", "pending", null -> "Awaiting payment"
        else -> statusRaw
    }

    AssistChip(
        onClick = {},
        label = { Text(label ?: "Awaiting payment") }
    )
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}