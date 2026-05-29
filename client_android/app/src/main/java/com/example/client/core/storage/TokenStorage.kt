package com.yogurtvpn.client.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yogurt_prefs")

class TokenStorage(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val VLESS_LINK_KEY = stringPreferencesKey("vless_link")
    }
    
    val token: Flow<String?> = context.dataStore.data.map{it[TOKEN_KEY]}

    val userId: Flow<String?> = context.dataStore.data
        .map { it[USER_ID_KEY] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { it[USER_EMAIL_KEY] }

    val vlessLink: Flow<String?> = context.dataStore.data
        .map { it[VLESS_LINK_KEY] }


    suspend fun saveAuth(token: String, userId: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_EMAIL_KEY] = email
        }
    }

    suspend fun saveVlessLink(link: String) {
        context.dataStore.edit { prefs ->
            prefs[VLESS_LINK_KEY] = link
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

