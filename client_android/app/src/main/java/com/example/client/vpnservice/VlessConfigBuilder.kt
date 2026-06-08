package com.yogurtvpn.client.vpnservice

import java.net.URI
import java.net.URLDecoder

object VlessConfigBuilder {

    /** Возвращает хост (IP или домен) VPN-сервера из vless-ссылки. */
    fun serverHost(vlessLink: String): String =
        URI(vlessLink.trim()).host
            ?: throw IllegalArgumentException("VLESS: нет хоста")

    fun build(vlessLink: String): String {
        val uri = URI(vlessLink.trim())
        val uuid = uri.userInfo
            ?: throw IllegalArgumentException("VLESS: нет UUID")
        val host = uri.host
            ?: throw IllegalArgumentException("VLESS: нет хоста")
        val port = uri.port.takeIf { it > 0 } ?: 443

        // Некоторые VLESS-ссылки используют '@' как разделитель параметров вместо '&'
        val params = (uri.rawQuery ?: "")
            .split("&", "@")
            .filter { "=" in it }
            .associate {
                val idx = it.indexOf("=")
                URLDecoder.decode(it.substring(0, idx), "UTF-8") to
                        URLDecoder.decode(it.substring(idx + 1), "UTF-8")
            }

        val security = params["security"] ?: "none"
        val sni = params["sni"] ?: host
        val network = params["type"] ?: "tcp"
        val flow = params["flow"] ?: ""
        val fingerprint = params["fp"] ?: "chrome"

        val streamSettings = buildStreamSettings(network, security, sni, fingerprint, params)

        return buildConfig(host, port, uuid, flow, streamSettings)
    }

    private fun buildStreamSettings(
        network: String,
        security: String,
        sni: String,
        fingerprint: String,
        params: Map<String, String>
    ): String {
        val securityPart = when (security) {
            "tls" -> """,
            "tlsSettings": {
                "serverName": "$sni",
                "allowInsecure": false,
                "fingerprint": "$fingerprint"
            }"""
            "reality" -> """,
            "realitySettings": {
                "fingerprint": "${fingerprint.ifEmpty { "chrome" }}",
                "serverName": "$sni",
                "publicKey": "${params["pbk"] ?: ""}",
                "shortId": "${params["sid"] ?: ""}",
                "spiderX": "${params["spx"] ?: "/"}"
            }"""
            else -> ""
        }

        val networkPart = when (network) {
            "ws" -> """,
            "wsSettings": {
                "path": "${params["path"] ?: "/"}",
                "headers": { "Host": "${params["host"] ?: sni}" }
            }"""
            "grpc" -> """,
            "grpcSettings": { "serviceName": "${params["serviceName"] ?: ""}" }"""
            "tcp" -> if (params["headerType"] == "http") """,
            "tcpSettings": {
                "header": {
                    "type": "http",
                    "request": {
                        "path": ["${params["path"] ?: "/"}"],
                        "headers": { "Host": ["${params["host"] ?: sni}"] }
                    }
                }
            }""" else ""
            else -> ""
        }

        return """{
            "network": "$network",
            "security": "$security"$securityPart$networkPart
        }"""
    }

    private fun buildConfig(
        host: String,
        port: Int,
        uuid: String,
        flow: String,
        streamSettings: String
    ) = """
{
    "log": { "loglevel": "warning", "access": "none" },
    "inbounds": [
        {
            "tag": "tun-in",
            "protocol": "tun",
            "settings": {}
        }
    ],
    "outbounds": [
        {
            "tag": "proxy",
            "protocol": "vless",
            "settings": {
                "vnext": [{
                    "address": "$host",
                    "port": $port,
                    "users": [{
                        "id": "$uuid",
                        "encryption": "none",
                        "flow": "$flow"
                    }]
                }]
            },
            "streamSettings": $streamSettings
        },
        {
            "tag": "direct",
            "protocol": "freedom",
            "settings": {}
        },
        {
            "tag": "block",
            "protocol": "blackhole",
            "settings": {}
        }
    ],
    "routing": {
        "domainStrategy": "AsIs",
        "rules": [
            {
                "type": "field",
                "ip": [
                    "10.0.0.0/8",
                    "172.16.0.0/12",
                    "192.168.0.0/16",
                    "127.0.0.0/8",
                    "169.254.0.0/16",
                    "224.0.0.0/4"
                ],
                "outboundTag": "direct"
            }
        ]
    }
}
""".trimIndent()
}
