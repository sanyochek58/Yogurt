package com.yogurtvpn.client.core.di

import com.example.client.feature.auth.data.AuthRepositoryImpl
import com.example.client.feature.auth.domain.AuthRepository
import com.yogurtvpn.client.core.network.HttpClientFactory
import com.yogurtvpn.client.core.network.YogurtApi
import com.yogurtvpn.client.core.network.dto.AuthResponse
import com.yogurtvpn.client.core.storage.TokenStorage
import com.yogurtvpn.client.feature.auth.presentation.AuthViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single { YogurtApi(get()) }

    single { TokenStorage(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    //single<HomeRepository> { HomeRepositoryImpl(get(), get()) }

    viewModel { AuthViewModel(get()) }
    //viewModel { HomeViewModel(get(), get()) }
    //viewModel { VpnViewModel(get(), get()) }
    

}