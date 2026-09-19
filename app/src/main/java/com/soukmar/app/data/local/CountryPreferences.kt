package com.soukmar.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Same "soukmar_prefs" DataStore instance as LocalePreferences (a different
 * key within it — see Context.soukmarPrefsDataStore's own comment for why
 * this must be the *same* declared instance, not a second one pointing at
 * the same file name) — separate from TokenManager's "soukmar_session" so
 * the chosen country survives logout, mirroring the web app's dedicated
 * "soukmar_country" localStorage key (independent of the auth session and of
 * the language). */
@Singleton
class CountryPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val COUNTRY = stringPreferencesKey("country")
    }

    suspend fun getCountry(): String? = context.soukmarPrefsDataStore.data.first()[Keys.COUNTRY]

    suspend fun setCountry(code: String) {
        context.soukmarPrefsDataStore.edit { it[Keys.COUNTRY] = code }
    }
}
