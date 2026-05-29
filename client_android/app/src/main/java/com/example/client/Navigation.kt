package com.yogurtvpn.client

sealed class Screen(val route: String){
    object Splash: Screen("splash")
    object Onboarding: Screen("onboarding")
    object Login: Screen("login")
    object Register: Screen("register")
    object Home: Screen("home")
    object RequestAccess: Screen("request_access")
    object PasteLink: Screen("paste_link")
}