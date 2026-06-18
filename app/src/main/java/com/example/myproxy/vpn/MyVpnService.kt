package com.example.myproxy.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.myproxy.R

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val tun2SocksManager by lazy { Tun2SocksManager(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startVpn()
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun startVpn() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Proxy running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        if (vpnInterface != null) return
        vpnInterface = Builder()
            .setSession("MyProxy")
            .setMtu(1500)
            .addAddress("10.0.0.2", 32)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .establish()

        val tunFd = vpnInterface?.fd ?: -1
        if (tunFd < 0 || !tun2SocksManager.start(tunFd)) {
            stopVpn()
        }
    }

    private fun stopVpn() {
        tun2SocksManager.stop()
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Proxy Service",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "myproxy_vpn_channel"
        private const val NOTIFICATION_ID = 101
        const val ACTION_CONNECT = "com.example.myproxy.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.myproxy.vpn.DISCONNECT"
    }
}
