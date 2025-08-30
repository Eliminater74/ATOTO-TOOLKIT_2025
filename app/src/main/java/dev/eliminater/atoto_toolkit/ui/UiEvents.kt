package dev.eliminater.atoto_toolkit.ui

sealed class UiEvent {
    data class Snackbar(val message: String, val actionLabel: String? = null) : UiEvent()
}
