package dev.eliminater.atoto_toolkit.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a single Preferences DataStore for the app.
private val Context.dataStore by preferencesDataStore(name = "atoto_settings")

/**
 * Small helper to read/write the selected theme mode (SYSTEM / LIGHT / DARK)
 * using Jetpack DataStore.
 */
object ThemePrefs {
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    /** Observe the current theme mode as a Flow (defaults to SYSTEM). */
    fun themeFlow(ctx: Context): Flow<ThemeMode> =
        ctx.dataStore.data.map { prefs ->
            when (prefs[KEY_THEME]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name  -> ThemeMode.DARK
                else                 -> ThemeMode.SYSTEM
            }
        }

    /** Persist a new theme mode. */
    suspend fun set(ctx: Context, mode: ThemeMode) {
        ctx.dataStore.edit { prefs -> prefs[KEY_THEME] = mode.name }
    }
}
