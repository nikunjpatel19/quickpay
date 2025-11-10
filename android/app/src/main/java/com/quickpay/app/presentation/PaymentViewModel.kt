package com.quickpay.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickpay.app.data.remote.ApiModule
import com.quickpay.app.data.remote.CreateLinkReq
import com.quickpay.app.data.remote.CreateLinkRes
import com.quickpay.app.data.remote.OrderDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class UiState(
    val amountText: String = "",
    val description: String = "",
    val currency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderId: String? = null,
    val checkoutUrl: String? = null,
    val orderStatus: String? = null // pending|succeeded|failed
)

class PaymentViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var pollJob: Job? = null

    fun onAmountChanged(t: String) { _state.value = _state.value.copy(amountText = t.filter { it.isDigit() }) }
    fun onDescChanged(t: String) { _state.value = _state.value.copy(description = t) }
    fun onCurrencyChanged(t: String) { _state.value = _state.value.copy(currency = t.uppercase()) }

    fun createLink() {
        val cents = _state.value.amountText.toIntOrNull() ?: 0
        if (cents <= 0) {
            _state.value = _state.value.copy(error = "Enter amount in cents (e.g., 999 for $9.99)")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val res = ApiModule.api.createLink(
                    CreateLinkReq(amountCents = cents, currency = _state.value.currency, description = _state.value.description)
                )
                if (res.isSuccessful) {
                    val body: CreateLinkRes? = res.body()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        orderId = body?.orderId,
                        checkoutUrl = body?.url,
                        orderStatus = "pending"
                    )
                    // start polling
                    startPolling()
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Create link failed: ${res.code()}")
                }
            } catch (e: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = "Network error")
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isLoading = false, error = "HTTP ${e.code()}")
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        val id = _state.value.orderId ?: return
        pollJob = viewModelScope.launch {
            repeat(120) { // ~10 minutes max
                try {
                    val res = ApiModule.api.getOrder(id)
                    if (res.isSuccessful) {
                        val order: OrderDto? = res.body()
                        _state.value = _state.value.copy(orderStatus = order?.status)
                        if (order?.status == "succeeded" || order?.status == "failed") {
                            return@launch
                        }
                    }
                } catch (_: Throwable) { /* ignore and keep polling */ }
                delay(3000)
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}