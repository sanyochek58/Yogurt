package com.yogurtvpn.client

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yogurtvpn.client.core.theme.GradientEnd
import com.yogurtvpn.client.core.theme.GradientMid
import com.yogurtvpn.client.core.theme.GradientStart
import com.yogurtvpn.client.core.theme.YogurtVPNTheme
import com.yogurtvpn.client.feature.auth.presentation.LoginScreen
import com.yogurtvpn.client.feature.auth.presentation.RegisterScreen
import com.yogurtvpn.client.feature.home.presentation.HomeScreen
import com.yogurtvpn.client.feature.vpn.presentation.PasteLinkScreen
import com.yogurtvpn.client.feature.vpn.presentation.VpnViewModel
import org.koin.androidx.compose.koinViewModel

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

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun YogurtVPNApp(){
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientStart, GradientMid, GradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(1500f, 1500f)
                )
            )
    ) {
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
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.PasteLink.route) {
            val homeEntry = remember(navController) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val vpnViewModel: VpnViewModel = koinViewModel(viewModelStoreOwner = homeEntry)
            PasteLinkScreen(
                onBack = { navController.popBackStack() },
                vpnViewModel = vpnViewModel
            )
        }
    }
    }
}