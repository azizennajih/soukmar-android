package com.soukmar.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "soukmar_session")

/** Persists the JWT + a light user snapshot, mirroring the frontend's
 * localStorage-based AuthService (soukmar_token / soukmar_session). */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    suspend fun getToken(): String? = context.dataStore.data.first()[Keys.TOKEN]

    suspend fun saveSession(token: String, userId: String, name: String, email: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_ROLE] = role
        }
    }

    suspend fun currentUserId(): String? = context.dataStore.data.first()[Keys.USER_ID]

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
