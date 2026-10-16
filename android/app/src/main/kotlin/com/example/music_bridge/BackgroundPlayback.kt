package com.example.music_bridge

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast

/**
 * 不建立視窗的背景點歌流程：先嘗試即時選曲，未連線才喚醒 Samsung Music。
 * 同一時間只保留最後一次點選；新點選會取消舊重試並結束舊廣播工作。
 */
object BackgroundPlayback {
    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var complete: (() -> Unit)? = null

    fun select(context: Context, song: SongSelection, onComplete: () -> Unit = {}) {
        // Handler 的工作在主執行緒排程；移除舊工作後，不會過一陣子播回舊點選。
        pending?.let { handler.removeCallbacks(it) }
        pending = null
        complete?.invoke()
        complete = onComplete
        val app = context.applicationContext
        Bridge.init(app)
        fun finish(message: String? = null) {
            pending = null
            val done = complete
            complete = null
            try {
                if (message != null) Toast.makeText(app, message, Toast.LENGTH_LONG).show()
                Bridge.inspect()
            // onComplete 通常是 BroadcastReceiver.PendingResult.finish。
            // 即使診斷寫檔失敗，也必須結束廣播，不讓系統一直等待。
            } finally { done?.invoke() }
        }
        if (!Bridge.permission()) {
            finish("請先開啟 MusicBridge 通知存取權")
            return
        }
        val started = SystemClock.elapsedRealtime()
        var wokePlayer = false
        val attempt = object : Runnable {
            override fun run() {
                // 先檢查期限，再嘗試播放，避免被系統延後的工作突然播放。
                if (SystemClock.elapsedRealtime() - started >= 8000) {
                    finish("未能在背景連線或找到這首歌，請確認 Samsung Music 的目前清單")
                    return
                }
                try {
                    if (Bridge.playSelection(song)) { finish(); return }
                    if (!wokePlayer && Bridge.snapshot()["connected"] != true) {
                        wokePlayer = true
                        val query = Intent(Intent.ACTION_MEDIA_BUTTON).setPackage(Bridge.PACKAGE)
                        // 限定 package 與明確接收器，避免把 PLAY 送給 YouTube 等其他播放器。
                        @Suppress("DEPRECATION")
                        val receiver = app.packageManager.queryBroadcastReceivers(query, 0)
                            .firstOrNull { it.activityInfo.exported && it.activityInfo.permission.isNullOrEmpty() }
                        if (receiver == null) { finish("Samsung Music 沒有可用的背景播放入口"); return }
                        for (action in listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP)) {
                            app.sendBroadcast(Intent(query)
                                .setClassName(receiver.activityInfo.packageName, receiver.activityInfo.name)
                                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                                .putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY)))
                        }
                    }
                    handler.postDelayed(this, 200)
                } catch (e: Exception) { finish(e.message ?: "無法在背景播放歌曲") }
            }
        }
        pending = attempt
        // Start immediately while handling the user-initiated widget broadcast.
        attempt.run()
    }
}
