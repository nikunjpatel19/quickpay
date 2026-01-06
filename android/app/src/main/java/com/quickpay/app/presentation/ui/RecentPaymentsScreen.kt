package com.quickpay.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickpay.app.data.remote.dto.OrderDto
import com.quickpay.app.presentation.RecentPaymentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentPaymentsScreen(vm: RecentPaymentsViewModel) {
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
                    Text("No payments yet", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.orders) { order -> OrderRow(order) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: OrderDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Order: ${order.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Amount: ${formatCents(order.amountCents, order.currency)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}