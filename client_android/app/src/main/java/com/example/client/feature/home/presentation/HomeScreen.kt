package com.yogurtvpn.client.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogurtvpn.client.core.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToPasteLink: () -> Unit,
    onNavigateToRequestAccess: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(YogurtPinkLight, Color(0xFFF3E8FF)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YogurtVPN",
                        style = MaterialTheme.typography.titleLarge,
                        color = YogurtPurpleDark
                    )
                    Text(
                        text = uiState.userEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(YogurtPurple, YogurtPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VpnButton(
                    state = uiState.vpnState,
                    enabled = uiState.vlessLink != null,
                    onClick = viewModel::toggleVpn
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = when (uiState.vpnState) {
                        VpnConnectionState.CONNECTED -> "Подключено"
                        VpnConnectionState.CONNECTING -> "Подключение..."
                        VpnConnectionState.DISCONNECTED -> "Отключено"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = when (uiState.vpnState) {
                        VpnConnectionState.CONNECTED -> StatusConnected
                        VpnConnectionState.CONNECTING -> StatusConnecting
                        VpnConnectionState.DISCONNECTED -> StatusDisconnected
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))

                AccessStatusCard(
                    uiState = uiState,
                    onRequestAccess = viewModel::requestAccess,
                    onPasteLink = onNavigateToPasteLink
                )
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = YogurtPurple
            )
        }
    }
}

@Composable
fun VpnButton(
    state: VpnConnectionState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val buttonColor by animateColorAsState(
        targetValue = when (state) {
            VpnConnectionState.CONNECTED -> StatusConnected
            VpnConnectionState.CONNECTING -> StatusConnecting
            VpnConnectionState.DISCONNECTED -> if (enabled) YogurtPurple else Gray
        },
        animationSpec = tween(500),
        label = "buttonColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (state == VpnConnectionState.CONNECTED) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.25f))
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = GrayBorder
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun AccessStatusCard(
    uiState: HomeUiState,
    onRequestAccess: () -> Unit,
    onPasteLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                !uiState.hasRequest -> {
                    Text(
                        text = "Доступ не запрошен",
                        style = MaterialTheme.typography.titleMedium,
                        color = Black
                    )
                    Text(
                        text = "Отправьте заявку администратору для получения доступа к VPN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRequestAccess,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YogurtPurple)
                    ) {
                        Text("Запросить доступ", color = Color.White)
                    }
                }

                uiState.accessStatus == "PENDING" -> {
                    Text(
                        text = "Заявка на рассмотрении",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnecting
                    )
                    Text(
                        text = "Администратор рассмотрит вашу заявку и отправит ссылку на email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center
                    )
                }

                uiState.accessStatus == "APPROVED" && uiState.vlessLink != null -> {
                    Text(
                        text = "Доступ одобрен",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected
                    )
                    Text(
                        text = "Нажмите кнопку выше чтобы подключиться",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = onPasteLink,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YogurtPurple)
                    ) {
                        Text("Обновить конфигурацию")
                    }
                }

                uiState.accessStatus == "APPROVED" -> {
                    Text(
                        text = "Доступ одобрен",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusConnected
                    )
                    Text(
                        text = "Вставьте VLESS-ссылку из письма",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onPasteLink,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YogurtPurple)
                    ) {
                        Text("Вставить ссылку", color = Color.White)
                    }
                }
            }
        }
    }
}