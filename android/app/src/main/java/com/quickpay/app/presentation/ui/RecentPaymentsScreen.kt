package com.quickpay.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickpay.app.data.remote.dto.OrderDto
import com.quickpay.app.presentation.RecentPaymentsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentPaymentsScreen(
    vm: RecentPaymentsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recent payments") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.load() }) { Text("Retry") }
                    }
                }

                state.orders.isEmpty() -> {
                    Text(
                        text = "No payments yet",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.orders) { order ->
                            OrderRow(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: OrderDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMoney(order.amountCents, order.currency),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusChip(order.status)
            }

            val note = order.note?.trim()
            if (!note.isNullOrEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Order: ${order.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(statusRaw: String) {
    val label = when (statusRaw.lowercase()) {
        "captured", "paid" -> "Paid"
        "failed" -> "Failed"
        "created", "pending" -> "Created"
        "authorized" -> "Authorized"
        else -> statusRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    AssistChip(
        onClick = {},
        label = { Text(label) }
    )
}

private fun formatMoney(amountCents: Long, currency: String): String {
    val value = (amountCents / 100.0)
    return "$" + String.format(Locale.US, "%.2f", value) + " " + currency
}