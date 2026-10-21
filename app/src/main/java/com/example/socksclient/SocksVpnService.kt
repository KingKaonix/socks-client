package com.example.socksclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class SocksVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var running = false
    private var proxyHost = "192.168.49.1"
    private var proxyPort = 1080

    companion object {
        const val TAG = "SocksClient"
        var instance: SocksVpnService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        proxyHost = intent?.getStringExtra("host") ?: "192.168.49.1"
        proxyPort = intent?.getIntExtra("port", 1080) ?: 1080

        startForeground(1, createNotification())
        setupVpn()
        return START_STICKY
    }

    private fun setupVpn() {
        val builder = Builder()
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .setSession("SocksClient")
            .setMtu(1500)

        vpnInterface = builder.establish()
        running = true

        val fd = vpnInterface?.fd ?: -1
        if (fd >= 0) {
            Thread {
                val socksServer = "$proxyHost:$proxyPort"
                val udpgwServer = "$proxyHost:7300"
                Tun2socks.startTun2socks("fd://$fd", socksServer, udpgwServer)
                Log.i(TAG, "tun2socks exited")
            }.start()
            Log.i(TAG, "VPN started with fd=$fd, proxy=$proxyHost:$proxyPort")
        } else {
            Log.e(TAG, "Failed to establish VPN")
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        val channelId = "socksclient"
        val channel = NotificationChannel(channelId, "SocksClient", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("SocksClient VPN")
            .setContentText("Connected to $proxyHost:$proxyPort")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        running = false
        Tun2socks.stop()
        vpnInterface?.close()
        instance = null
        super.onDestroy()
    }
}
