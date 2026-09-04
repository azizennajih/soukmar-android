package com.soukmar.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localeDataStore by preferencesDataStore(name = "soukmar_prefs")

/** Separate from TokenManager's "soukmar_session" store on purpose — the
 * chosen language must survive logout, mirroring the web app's dedicated
 * "soukmar_lang" localStorage key (independent of the auth session). */
@Singleton
class LocalePreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
    }

    suspend fun getLanguage(): String? = context.localeDataStore.data.first()[Keys.LANGUAGE]

    suspend fun setLanguage(code: String) {
        context.localeDataStore.edit { it[Keys.LANGUAGE] = code }
    }
}
