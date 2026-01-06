package com.quickpay.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickpay.app.data.remote.ApiModule
import com.quickpay.app.data.remote.dto.CreateLinkReq
import com.quickpay.app.data.remote.dto.OrderDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

data class UiState(
    val amountText: String = "",     // now dollars string, e.g. "9.99"
    val description: String = "",
    val currency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderId: String? = null,
    val checkoutUrl: String? = null,
    val orderStatus: String? = null  // CREATED | CAPTURED | FAILED ...
)

class PaymentViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var pollJob: Job? = null

    // allow digits + one dot, trim extra dots
    fun onAmountChanged(t: String) {
        val filtered = buildString {
            var dotSeen = false
            for (ch in t) {
                when {
                    ch.isDigit() -> append(ch)
                    ch == '.' && !dotSeen -> {
                        dotSeen = true
                        append(ch)
                    }
                }
            }
        }
        _state.value = _state.value.copy(amountText = filtered)
    }

    fun onDescChanged(t: String) {
        _state.value = _state.value.copy(description = t)
    }

    fun onCurrencyChanged(t: String) {
        val cleaned = t.filter { it.isLetter() }.take(3).uppercase()
        _state.value = _state.value.copy(currency = cleaned)
    }

    fun createLink() {
        val amountDollars = _state.value.amountText.trim()
        val cents = dollarsToCentsOrNull(amountDollars)

        if (cents == null || cents <= 0L) {
            _state.value = _state.value.copy(error = "Enter a valid amount (example: 9.99)")
            return
        }

        if (_state.value.currency.length != 3) {
            _state.value = _state.value.copy(error = "Currency must be 3 letters (example: USD)")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = ApiModule.api.createLink(
                    CreateLinkReq(
                        amountCents = cents,
                        currency = _state.value.currency,
                        description = _state.value.description.ifBlank { null }
                    )
                )

                if (response.isSuccessful) {
                    val link = response.body()
                    if (link == null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Empty response from server"
                        )
                        return@launch
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        orderId = link.orderId,
                        checkoutUrl = link.url,
                        orderStatus = link.status
                    )

                    startPolling(link.orderId)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Request failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Request failed"
                )
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

                    if (uiStatus.equals("CAPTURED", true) || uiStatus.equals("FAILED", true)) {
                        return@launch
                    }
                } catch (_: Throwable) {
                    // keep polling
                }

                delay(3000)
            }

            _state.value = _state.value.copy(error = "Timed out waiting for payment update")
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }

    private fun dollarsToCentsOrNull(input: String): Long? {
        if (input.isBlank()) return null
        return try {
            val bd = BigDecimal(input)
                .setScale(2, RoundingMode.HALF_UP) // 9.9 -> 9.90, 9.999 -> 10.00
            bd.movePointRight(2).longValueExact()
        } catch (_: Exception) {
            null
        }
    }
}