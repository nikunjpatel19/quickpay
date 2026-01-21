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

enum class CreateFlowStep { INPUT, PAYMENT }
enum class Currency(val code: String) { USD("USD"), CAD("CAD") }

data class UiState(
    val step: CreateFlowStep = CreateFlowStep.INPUT,
    val amountText: String = "",     // dollars string, e.g. "9.99"
    val description: String = "",
    val note: String = "",       // NEW
    val currency: Currency = Currency.USD, // NEW (no blank typing)
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

    fun onNoteChanged(t: String) { // NEW
        _state.value = _state.value.copy(note = t)
    }

    fun onCurrencySelected(currency: Currency) { // NEW
        _state.value = _state.value.copy(currency = currency)
    }

    fun createLink() {
        val amountDollars = _state.value.amountText.trim()
        val cents = dollarsToCentsOrNull(amountDollars)

        if (cents == null || cents <= 0L) {
            _state.value = _state.value.copy(error = "Enter a valid amount (example: 9.99)")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = ApiModule.api.createLink(
                    CreateLinkReq(
                        amountCents = cents,
                        currency = _state.value.currency.code,
                        description = _state.value.description.ifBlank { null },
                        note = _state.value.note.ifBlank { null }
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
                        step = CreateFlowStep.PAYMENT,
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
            repeat(120) {
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

    fun startNewPayment() {
        pollJob?.cancel()
        _state.value = UiState()
    }

    fun editDetails() {
        pollJob?.cancel()
        _state.value = _state.value.copy(step = CreateFlowStep.INPUT)
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
            val bd = BigDecimal(input).setScale(2, RoundingMode.HALF_UP)
            bd.movePointRight(2).longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    fun cancelCurrentOrder() {
        val id = _state.value.orderId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val res = ApiModule.api.cancelOrder(id)
                if (res.isSuccessful) {
                    pollJob?.cancel()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        orderStatus = "FAILED"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Cancel failed (${res.code()})"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Cancel failed"
                )
            }
        }
    }
}