package dev.eliminater.atoto_toolkit.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// Use a unique name to avoid collisions with any libraries or older files
val Context.appDataStore by preferencesDataStore(name = "atoto_settings")
