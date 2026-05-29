package com.yogurtvpn.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yogurtvpn.client.core.theme.YogurtVPNTheme
import com.yogurtvpn.client.feature.auth.presentation.LoginScreen
import com.yogurtvpn.client.feature.auth.presentation.RegisterScreen
import com.yogurtvpn.client.feature.home.presentation.HomeScreen
import com.yogurtvpn.client.feature.vpn.presentation.PasteLinkScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            YogurtVPNTheme {
                YogurtVPNApp()
            }
        }
    }
}

@Composable
fun YogurtVPNApp(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ){
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPasteLink = {
                    navController.navigate(Screen.PasteLink.route)
                },
                onNavigateToRequestAccess = {
                    navController.navigate(Screen.RequestAccess.route)
                }
            )
        }
        composable(Screen.PasteLink.route) {
            PasteLinkScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }


}