package com.yogurtvpn.client.vpnservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import libv2ray.CoreCallbackHandler
import libv2ray.Libv2ray
import java.net.InetAddress

class YogurtVpnService : VpnService() {

    companion object {
        private const val TAG = "YogurtVPN"
        private const val CHANNEL_ID = "yogurt_vpn_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_CONNECT = "com.yogurtvpn.client.CONNECT"
        const val ACTION_DISCONNECT = "com.yogurtvpn.client.DISCONNECT"
        const val EXTRA_VLESS_LINK = "vless_link"

        var isRunning = false
            private set
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var coreController: libv2ray.CoreController? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val coreCallback = object : CoreCallbackHandler {
        override fun startup(): Long = 0L
        override fun shutdown(): Long = 0L
        override fun onEmitStatus(level: Long, msg: String): Long = 0L
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_CONNECT -> {
                val vlessLink = intent.getStringExtra(EXTRA_VLESS_LINK) ?: ""
                startForegroundNotification()
                serviceScope.launch { connect(vlessLink) }
                START_STICKY
            }
            ACTION_DISCONNECT -> {
                disconnect()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "YogurtVPN", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("YogurtVPN")
            .setContentText("VPN активен")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun connect(vlessLink: String) {
        try {
            // Резолвим IP VPN-сервера, чтобы исключить его из маршрутов TUN.
            // Иначе исходящий сокет xray к серверу заворачивается обратно в TUN → петля.
            val serverHost = VlessConfigBuilder.serverHost(vlessLink)
            val serverIp = withContext(Dispatchers.IO) {
                try { InetAddress.getByName(serverHost).hostAddress } catch (e: Exception) { null }
            }
            Log.d(TAG, "Сервер: $serverHost → $serverIp")

            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addAddress("fd00::1", 128)
                .addDnsServer("8.8.8.8")
                .addDnsServer("2001:4860:4860::8888")
                .setSession("YogurtVPN")
                .setMtu(1500)

            // Исключаем приватные сети (LAN идёт напрямую, нет флуда Chromecast)
            // и IP самого сервера (иначе петля).
            val excludes = mutableListOf(
                ipv4ToLong("10.0.0.0")    to 8,
                ipv4ToLong("172.16.0.0")  to 12,
                ipv4ToLong("192.168.0.0") to 16,
                ipv4ToLong("127.0.0.0")   to 8,
                ipv4ToLong("169.254.0.0") to 16,
                ipv4ToLong("100.64.0.0")  to 10,  // CGNAT (мобильные операторы)
                ipv4ToLong("224.0.0.0")   to 3    // multicast + reserved
            )
            if (serverIp != null && isIpv4(serverIp)) {
                excludes += ipv4ToLong(serverIp) to 32
            }

            for ((base, prefix) in routesExcluding(excludes)) {
                builder.addRoute(base, prefix)
            }
            // Весь IPv6 через TUN (xml-проксируется на IPv4-сервер, утечки нет)
            builder.addRoute("::", 0)

            vpnInterface = builder.establish() ?: run {
                Log.e(TAG, "establish() вернул null")
                isRunning = false
                return
            }

            Libv2ray.initCoreEnv(applicationContext.filesDir.absolutePath, "")

            val config = VlessConfigBuilder.build(vlessLink)
            coreController = Libv2ray.newCoreController(coreCallback)
            coreController?.startLoop(config, vpnInterface!!.fd)

            isRunning = true
            Log.d(TAG, "VPN подключён")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка подключения: ${e.message}", e)
            isRunning = false
            disconnect()
        }
    }

    private fun disconnect() {
        try { coreController?.stopLoop() } catch (_: Exception) {}
        coreController = null

        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null

        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }

    // ───────── Вычисление IPv4-маршрутов 0.0.0.0/0 минус исключённые блоки ─────────

    private fun ipv4ToLong(ip: String): Long {
        val parts = ip.split(".")
        return (parts[0].toLong() shl 24) or (parts[1].toLong() shl 16) or
                (parts[2].toLong() shl 8) or parts[3].toLong()
    }

    private fun longToIp(v: Long): String =
        "${(v shr 24) and 0xFF}.${(v shr 16) and 0xFF}.${(v shr 8) and 0xFF}.${v and 0xFF}"

    private fun isIpv4(ip: String): Boolean =
        ip.count { it == '.' } == 3 && ip.none { it == ':' }

    /**
     * Рекурсивно делит 0.0.0.0/0 и возвращает минимальный набор подсетей,
     * покрывающих всё адресное пространство КРОМЕ переданных блоков.
     */
    private fun routesExcluding(excludes: List<Pair<Long, Int>>): List<Pair<String, Int>> {
        val result = mutableListOf<Pair<String, Int>>()

        fun recurse(base: Long, prefix: Int) {
            val size = 1L shl (32 - prefix)
            val start = base
            val end = base + size - 1

            for ((eb, ep) in excludes) {
                val es = eb
                val ee = eb + (1L shl (32 - ep)) - 1
                if (es <= start && ee >= end) return            // блок полностью исключён
            }
            val overlaps = excludes.any { (eb, ep) ->
                val es = eb; val ee = eb + (1L shl (32 - ep)) - 1
                !(ee < start || es > end)
            }
            if (!overlaps) { result.add(longToIp(base) to prefix); return }
            if (prefix == 32) return                            // одиночный исключённый адрес

            val half = 1L shl (32 - prefix - 1)
            recurse(base, prefix + 1)
            recurse(base + half, prefix + 1)
        }

        recurse(0L, 0)
        return result
    }
}
