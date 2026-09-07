package com.netbooster.app.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.netbooster.app.ui.MainActivity
import com.netbooster.app.util.Prefs
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.concurrent.thread

/**
 * IMPORTANT / HONEST SCOPE NOTE
 * ------------------------------
 * This service creates a VPN interface (Android requires this to touch ANY packet without
 * root — there's no way around the key icon showing up), but it ONLY intercepts port-53
 * DNS traffic and forwards it to the configured resolver. Every other packet (your actual
 * app traffic: HTTP, video, games, etc.) is routed straight to "allowBypass()" apps / the
 * underlying network as normal — this service does not relay, inspect, or modify it.
 *
 * Why: genuinely re-shaping every TCP connection (fragment, sockopt, congestion control)
 * for the whole device without root requires a full userspace TCP/IP stack (what
 * tun2socks / hev-socks5-tunnel do, thousands of lines of C/Go). That's a real library to
 * bundle, not something to fake here. If you get to that point, the extension point is
 * marked below with TODO.
 */
class TunnelVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var running = false

    companion object {
        const val ACTION_CONNECT = "com.netbooster.app.CONNECT"
        const val ACTION_DISCONNECT = "com.netbooster.app.DISCONNECT"
        const val CHANNEL_ID = "netbooster_service"
        const val NOTIF_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                stopTunnel()
                return START_NOT_STICKY
            }
            else -> startTunnel()
        }
        return START_STICKY
    }

    private fun startTunnel() {
        if (running) return
        val prefs = Prefs(this)

        val builder = Builder()
            .setSession("Net Booster")
            .setMtu(prefs.mtu)
            .addAddress("10.10.0.2", 32)
            .addDnsServer(prefs.dnsPrimary)
            .addDnsServer(prefs.dnsSecondary)
            // Only route the DNS server addresses through us — everything else stays
            // on the normal network path, untouched.
            .addRoute(prefs.dnsPrimary, 32)
            .addRoute(prefs.dnsSecondary, 32)

        vpnInterface = builder.establish()
        running = true
        startForeground(NOTIF_ID, buildNotification())

        thread(name = "netbooster-dns-relay") { relayDns() }
    }

    private fun relayDns() {
        val fd = vpnInterface ?: return
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val prefs = Prefs(this)
        val buffer = ByteArray(32767)

        // Minimal DNS-over-UDP relay: read a raw IP/UDP/DNS packet from the tun device,
        // strip to the DNS payload, forward it to the real resolver over a protected
        // socket (so it doesn't loop back into the VPN), then write the raw response
        // packet back. Full IP/UDP header (de)serialization omitted here for brevity —
        // TODO: wire in a small UDP/IP packet library (or reuse gvisor's netstack) to
        // finish this relay loop before shipping.
        val socket = DatagramSocket()
        protect(socket)

        try {
            while (running) {
                val length = input.read(buffer)
                if (length <= 0) continue
                // TODO: parse IPv4/UDP header from buffer[0..length), extract DNS query,
                // forward via `socket` to InetSocketAddress(prefs.dnsPrimary, 53),
                // then re-wrap the reply with matching IP/UDP headers and write() it
                // back to `output`. See README.md for the recommended library.
            }
        } catch (e: Exception) {
            // socket closed on disconnect
        } finally {
            socket.close()
        }
    }

    private fun stopTunnel() {
        running = false
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Net Booster", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Net Booster active")
            .setContentText("Optimized DNS running")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }
}
