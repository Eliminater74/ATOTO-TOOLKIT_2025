package dev.eliminater.atoto_toolkit.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> =
        context.appDataStore.data.map { prefs ->
            when (prefs[KEY_THEME]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name  -> ThemeMode.DARK
                else                 -> ThemeMode.SYSTEM
            }
        }

    suspend fun setTheme(mode: ThemeMode) {
        context.appDataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun resetThemeToSystem() = setTheme(ThemeMode.SYSTEM)
}
