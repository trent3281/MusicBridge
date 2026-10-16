package com.example.music_bridge

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService

/**
 * 由 Android 系統綁定的服務，與 Flutter StatefulWidget 生命週期不同。
 * 通知存取權讓它能觀察其他 App 的媒體工作階段；只處理 Samsung Music。
 * 連線時啟動 Bridge 補查，斷線時清理並請求重綁。
 */
class MusicListener : NotificationListenerService() {
    companion object {
        private var bound = false
        private var lastRebind = -30000L
        fun ensureConnected(context: Context) {
            val now = android.os.SystemClock.elapsedRealtime()
            if (!bound && Bridge.permission() && now - lastRebind >= 30000L) {
                lastRebind = now
                runCatching { requestRebind(ComponentName(context, MusicListener::class.java)) }
            }
        }
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        MusicWidget.updateAll(this)
    }
    override fun onNotificationPosted(sbn: android.service.notification.StatusBarNotification) {
        if (sbn.packageName == Bridge.PACKAGE) {
            Bridge.init(this)
            Bridge.onPlayerNotificationChanged()
        }
    }
    override fun onNotificationRemoved(sbn: android.service.notification.StatusBarNotification) {
        if (sbn.packageName == Bridge.PACKAGE) {
            Bridge.init(this)
            Bridge.onPlayerNotificationChanged()
        }
    }
    override fun onListenerConnected() {
        bound = true
        Bridge.init(this); Bridge.connect(); Bridge.startMonitoring(); MusicWidget.updateAll(this)
    }
    override fun onListenerDisconnected() {
        bound = false
        Bridge.init(this)
        Bridge.stop()
        ensureConnected(this)
        val appContext = applicationContext
        Handler(Looper.getMainLooper()).postDelayed({ ensureConnected(appContext) }, 30000)
    }
    override fun onDestroy() {
        bound = false
        Bridge.init(this)
        Bridge.stop()
        super.onDestroy()
    }
}
