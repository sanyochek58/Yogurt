package com.yogurtvpn.client.feature.home.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yogurtvpn.client.core.theme.*
import com.yogurtvpn.client.feature.auth.presentation.AuthViewModel
import com.yogurtvpn.client.feature.auth.presentation.GradientButton
import com.yogurtvpn.client.feature.vpn.presentation.VpnViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToPasteLink: () -> Unit,
    onNavigateToRequestAccess: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    vpnViewModel: VpnViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vpnUiState by vpnViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var profileExpanded by remember { mutableStateOf(false) }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vpnViewModel.onPermissionResult(
            result.resultCode == Activity.RESULT_OK,
            context
        )
    }

    LaunchedEffect(vpnUiState.needsPermission) {
        if (vpnUiState.needsPermission && vpnUiState.pendingPrepareIntent != null) {
            vpnPermissionLauncher.launch(vpnUiState.pendingPrepareIntent!!)
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val buttonSize = when {
        screenHeight < 650.dp -> 180.dp
        screenHeight < 800.dp -> 220.dp
        else -> 260.dp
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HeaderCenter(
                onProfileClick = { profileExpanded = !profileExpanded }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = profileExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
            ) {
                Column {
                    ProfileCard(
                        email = uiState.userEmail,
                        vlessLink = uiState.vlessLink,
                        onLogout = {
                            authViewModel.logout()
                            onLogout()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VpnPulseButton(
                    isConnected = vpnUiState.isConnected,
                    isConnecting = vpnUiState.isConnecting,
                    enabled = !uiState.vlessLink.isNullOrBlank(),
                    size = buttonSize,
                    onClick = {
                        if (vpnUiState.isConnected) {
                            vpnViewModel.disconnect(context)
                        } else {
                            vpnViewModel.connect(context)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                StatusLabel(
                    isConnected = vpnUiState.isConnected,
                    isConnecting = vpnUiState.isConnecting
                )
            }

            AccessStatusCard(
                uiState = uiState,
                onRequestAccess = viewModel::requestAccess,
                onPasteLink = onNavigateToPasteLink,
                onRefresh = viewModel::refreshVpnConfig,
                onRefreshStatus = viewModel::refresh
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = YogurtPurpleDark
            )
        }
    }
}

@Composable
fun HeaderCenter(onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Yogurt VPN",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = YogurtPurpleDark
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(YogurtPurple, YogurtPink)
                    )
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Профиль",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun ProfileCard(
    email: String?,
    vlessLink: String?,
    onLogout: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(YogurtPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = YogurtPurpleDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Аккаунт",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = email ?: "—",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(YogurtPink.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = YogurtPinkDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VLESS-ссылка",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = vlessLink ?: "Не настроена",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (vlessLink != null) Black else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (vlessLink != null) {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(vlessLink))
                        }
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Копировать",
                            tint = YogurtPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StatusDisconnected
                )
            ) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти из аккаунта", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun VpnPulseButton(
    isConnected: Boolean,
    isConnecting: Boolean,
    enabled: Boolean,
    size: androidx.compose.ui.unit.Dp = 240.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            isConnected -> StatusConnected
            isConnecting -> StatusConnecting
            enabled -> YogurtPurple
            else -> Gray
        },
        animationSpec = tween(600),
        label = "color"
    )

    val innerButtonSize = size * 0.58f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier
                .size(size * 0.83f)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .size(size * 0.71f)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.28f))
        )
        Box(
            modifier = Modifier
                .size(innerButtonSize)
                .shadow(24.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (isConnected || isConnecting)
                        Brush.radialGradient(listOf(buttonColor, buttonColor))
                    else
                        Brush.linearGradient(listOf(YogurtPink, YogurtPurple))
                )
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(innerButtonSize * 0.32f),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Outlined.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(innerButtonSize * 0.4f),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun StatusLabel(isConnected: Boolean, isConnecting: Boolean) {
    val text = when {
        isConnecting -> "Подключение..."
        isConnected -> "Соединение защищено"
        else -> "Не подключено"
    }
    val color = when {
        isConnecting -> StatusConnecting
        isConnected -> StatusConnected
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun AccessStatusCard(
    uiState: HomeUiState,
    onRequestAccess: () -> Unit,
    onPasteLink: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                !uiState.hasRequest -> {
                    Text(
                        text = "Запросите доступ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Text(
                        text = "Отправьте заявку администратору чтобы получить VLESS-ссылку",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GradientButton(
                        text = "Отправить заявку",
                        onClick = onRequestAccess
                    )
                }

                uiState.accessStatus == "PENDING" -> {
                    Text(
                        text = "Заявка на рассмотрении",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusConnecting
                    )
                    Text(
                        text = "Администратор рассмотрит вашу заявку и отправит ссылку на email",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onRefreshStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = YogurtPurpleDark
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Обновить статус", fontSize = 13.sp)
                    }
                }

                uiState.accessStatus == "APPROVED" && !uiState.vlessLink.isNullOrBlank() -> {
                    Text(
                        text = "Конфигурация активна",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusConnected
                    )
                    Text(
                        text = "Нажмите кнопку выше чтобы подключиться к VPN",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = YogurtPurpleDark
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Обновить", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onPasteLink,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = YogurtPurpleDark
                            )
                        ) {
                            Text("Сменить", fontSize = 12.sp)
                        }
                    }
                }

                uiState.accessStatus == "APPROVED" -> {
                    Text(
                        text = "Доступ одобрен",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusConnected
                    )
                    Text(
                        text = "Вставьте VLESS-ссылку которая пришла на email",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GradientButton(
                        text = "Вставить ссылку",
                        onClick = onPasteLink
                    )
                }
            }
        }
    }
}