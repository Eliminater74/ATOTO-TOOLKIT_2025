package dev.eliminater.atoto_toolkit.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.eliminater.atoto_toolkit.ui.UiEvent
import dev.eliminater.atoto_toolkit.ui.UiEventBus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 👉 Use the correct import for ThemeMode based on where you declared it:
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
// or: import dev.eliminater.atoto_toolkit.settings.ThemeMode

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val theme = repo.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM
    )

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        repo.setTheme(mode)
        UiEventBus.emit(
            UiEvent.Snackbar("Theme set to ${mode.name.lowercase().replaceFirstChar { it.uppercase() }}")
        )
    }

    fun resetTheme() = viewModelScope.launch {
        repo.resetThemeToSystem()
        UiEventBus.emit(UiEvent.Snackbar("Theme reset to System"))
    }

    companion object {
        fun factory(repo: SettingsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(repo) as T
            }
        }
    }
}
