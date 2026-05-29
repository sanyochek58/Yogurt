package com.yogurtvpn.client.vpnservice

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class YogurtVpnService: VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.yogurtvpn.client.CONNECT"
        const val ACTION_DISCONNECT = "com.yogurtvpn.client.DISCONNECT"
        const val EXTRA_VLESS_LINK = "vless_link"

        var isRunning = false
            private set
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_CONNECT -> {
                val vlessLink = intent.getStringExtra(EXTRA_VLESS_LINK) ?: ""
                connect(vlessLink)
                START_STICKY
            }
            ACTION_DISCONNECT -> {
                disconnect()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    private fun connect(vlessLink: String) {
        serviceScope.launch {
            try {
                vpnInterface = Builder()
                    .addAddress("10.0.0.2", 24)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("8.8.4.4")
                    .addRoute("0.0.0.0", 0)
                    .setSession("YogurtVPN")
                    .setMtu(1500)
                    .establish()

                isRunning = vpnInterface != null
            } catch (e: Exception) {
                isRunning = false
            }
        }
    }

    private fun disconnect() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            isRunning = false
        } catch (e: Exception) { }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }

}