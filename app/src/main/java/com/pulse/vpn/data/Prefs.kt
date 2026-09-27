package com.pulse.vpn.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("pulse")

data class AccountState(
    val trialStart: Long = 0L,
    val premiumUntil: Long = 0L,
    val consentExplained: Boolean = false,
    val selectedServerId: String = "auto"
)

object Prefs {
    private val K_TRIAL = longPreferencesKey("trial_start")
    private val K_PREMIUM = longPreferencesKey("premium_until")
    private val K_CONSENT = booleanPreferencesKey("consent_explained")
    private val K_SERVER = stringPreferencesKey("server_id")

    fun state(ctx: Context): Flow<AccountState> = ctx.ds.data.map { p ->
        AccountState(p[K_TRIAL] ?: 0L, p[K_PREMIUM] ?: 0L, p[K_CONSENT] ?: false, p[K_SERVER] ?: "auto")
    }

    suspend fun ensureTrial(ctx: Context) {
        ctx.ds.edit { if ((it[K_TRIAL] ?: 0L) == 0L) it[K_TRIAL] = System.currentTimeMillis() }
    }

    suspend fun addPremium(ctx: Context, days: Int) {
        val cur = ctx.ds.data.first()[K_PREMIUM] ?: 0L
        val base = maxOf(System.currentTimeMillis(), cur)
        ctx.ds.edit { it[K_PREMIUM] = base + days * 86_400_000L }
    }

    suspend fun setConsent(ctx: Context) { ctx.ds.edit { it[K_CONSENT] = true } }
    suspend fun setServer(ctx: Context, id: String) { ctx.ds.edit { it[K_SERVER] = id } }
}
