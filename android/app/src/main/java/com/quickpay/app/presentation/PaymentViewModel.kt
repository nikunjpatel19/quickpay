package com.quickpay.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickpay.app.data.remote.ApiModule
import com.quickpay.app.data.remote.dto.CreateLinkReq
import com.quickpay.app.data.remote.dto.OrderDto
import com.quickpay.app.data.remote.dto.PaymentLinkDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val amountText: String = "",
    val description: String = "",
    val currency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderId: String? = null,
    val checkoutUrl: String? = null,
    val orderStatus: String? = null      // CREATED | CAPTURED | FAILED ...
)

class PaymentViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var pollJob: Job? = null

    fun onAmountChanged(t: String) { _state.value = _state.value.copy(amountText = t.filter { it.isDigit() }) }
    fun onDescChanged(t: String) { _state.value = _state.value.copy(description = t) }
    fun onCurrencyChanged(t: String) { _state.value = _state.value.copy(currency = t.uppercase()) }

    fun createLink() {
        val cents = _state.value.amountText.toLongOrNull() ?: 0L
        if (cents <= 0L) {
            _state.value = _state.value.copy(error = "Enter amount in cents (e.g., 999 for $9.99)")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val link: PaymentLinkDto = ApiModule.api.createLink(
                    CreateLinkReq(
                        amountCents = cents,
                        currency = _state.value.currency,
                        description = _state.value.description.ifBlank { null }
                    )
                )

                // orderId == link.id (we create an order with same id on server)
                _state.value = _state.value.copy(
                    isLoading = false,
                    orderId = link.id,
                    checkoutUrl = link.checkoutUrl,         // may be null until Finix
                    orderStatus = "CREATED"                  // initial UI status
                )

                startPolling(link.id)

            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Request failed")
            }
        }
    }

    private fun startPolling(id: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(120) { // ~6 minutes at 3s each
                try {
                    val order: OrderDto = ApiModule.api.getOrder(id)
                    val uiStatus = order.status
                    _state.value = _state.value.copy(orderStatus = uiStatus)

                    // consider CAPTURED == success; FAILED == failure
                    if (uiStatus.equals("CAPTURED", true) ||
                        uiStatus.equals("FAILED", true)) {
                        return@launch
                    }
                } catch (_: Throwable) { /* keep polling */ }

                delay(3000)
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}