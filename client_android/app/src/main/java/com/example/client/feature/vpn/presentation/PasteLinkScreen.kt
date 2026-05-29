package com.yogurtvpn.client.feature.vpn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogurtvpn.client.core.theme.*
import com.yogurtvpn.client.feature.home.presentation.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasteLinkScreen(
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var link by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

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
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = YogurtPurpleDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Вставить ссылку",
                style = MaterialTheme.typography.headlineMedium,
                color = YogurtPurpleDark
            )

            Text(
                text = "Вставьте VLESS-ссылку из письма которое пришло на вашу почту",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("VLESS-ссылка") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YogurtPurple,
                            focusedLabelColor = YogurtPurple,
                            cursorColor = YogurtPurple
                        ),
                        placeholder = {
                            Text(
                                "vless://...",
                                color = Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { link = it }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = YogurtPurple
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Вставить из буфера обмена")
                    }

                    Button(
                        onClick = {
                            if (link.startsWith("vless://")) {
                                homeViewModel.onVlessLinkPasted(link)
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YogurtPurple
                        ),
                        enabled = link.startsWith("vless://")
                    ) {
                        Text(
                            "Сохранить и подключить",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = YogurtPurple.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "💡 Ссылка начинается с vless:// и была отправлена на вашу почту после одобрения заявки",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = YogurtPurpleDark,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}