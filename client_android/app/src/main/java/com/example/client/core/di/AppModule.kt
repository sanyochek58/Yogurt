package com.yogurtvpn.client.core.di

import com.yogurtvpn.client.core.network.HttpClientFactory
import com.yogurtvpn.client.core.network.YogurtApi
import com.yogurtvpn.client.core.network.dto.AuthResponse
import com.yogurtvpn.client.core.storage.TokenStorage
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single { YogurtApi(get()) }

    single { TokenStorage(androidContext()) }

}