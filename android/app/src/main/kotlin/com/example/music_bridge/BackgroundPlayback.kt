package com.example.music_bridge

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
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
    // 連線生命週期獨立於單次廣播，避免連續點擊時先解除綁定、再使用失效的 session。
    private var serviceConnection: ServiceConnection? = null
    private var serviceBoundAt = 0L
    private var releaseService: Runnable? = null

    fun select(context: Context, song: SongSelection, onComplete: () -> Unit = {}) =
        start(context, song, "resume", onComplete)

    /** 播放鍵只恢復播放器目前曲目，不拿快取歌名重新選曲，也不開 Activity。 */
    fun resume(context: Context, onComplete: () -> Unit = {}) =
        start(context, null, "resume", onComplete)

    /** 離線的上一首、下一首及模式控制，共用同一個背景連線生命週期。 */
    fun command(context: Context, command: String, onComplete: () -> Unit = {}) {
        require(command in listOf("previous", "next", "repeat", "shuffle"))
        start(context, null, command, onComplete)
    }

    private fun start(context: Context, song: SongSelection?, command: String, onComplete: () -> Unit) {
        // Handler 的工作在主執行緒排程；移除舊工作後，不會過一陣子播回舊點選。
        pending?.let { handler.removeCallbacks(it) }
        pending = null
        complete?.invoke()
        val app = context.applicationContext
        releaseService?.let { handler.removeCallbacks(it) }
        complete = onComplete
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
            } finally {
                // Samsung Music 仍可能在非同步儲存佇列；短暫保留服務也讓下一次點擊沿用連線。
                releaseService = Runnable {
                    serviceConnection?.let { runCatching { app.unbindService(it) } }
                    serviceConnection = null
                }.also { handler.postDelayed(it, 30000) }
                done?.invoke()
            }
        }
        if (!Bridge.permission()) {
            finish(app.getString(R.string.permission_required))
            return
        }
        val started = SystemClock.elapsedRealtime()
        var wokePlayer = false
        var submittedAt = 0L
        var originalTitle: Any? = null
        var originalActiveId: Any? = null
        var originalMode: Any? = null
        val restorePause = command in listOf("repeat", "shuffle") && Bridge.snapshot()["playing"] != true
        val attempt = object : Runnable {
            override fun run() {
                // 先檢查期限，再嘗試播放，避免被系統延後的工作突然播放。
                if (SystemClock.elapsedRealtime() - started >= 8000) {
                    finish(app.getString(if (song == null) R.string.background_error else R.string.background_timeout))
                    return
                }
                try {
                    val snapshot = Bridge.snapshot()
                    if (submittedAt != 0L) {
                        // transportControls 回傳僅代表送出，播放必須等到實際狀態確認。
                        val confirmed = when (command) {
                            "resume" -> snapshot["playing"] == true
                            "repeat", "shuffle" -> snapshot[command] != originalMode
                            else -> snapshot["title"] != originalTitle ||
                                (snapshot["activeId"] != originalActiveId && snapshot["activeId"] != -1L)
                        }
                        if (confirmed) {
                            if (restorePause && snapshot["playing"] == true) Bridge.command("toggle")
                            finish(); return
                        }
                        handler.postDelayed(this, 200)
                        return
                    }
                    val waitingForService = serviceConnection != null &&
                        (SystemClock.elapsedRealtime() - serviceBoundAt < 1200 || snapshot["connecting"] == true)
                    val accepted = when {
                        waitingForService || snapshot["connecting"] == true -> false
                        song != null -> Bridge.playSelection(song)
                        command == "resume" -> Bridge.resumePlayback()
                        else -> Bridge.backgroundCommand(command)
                    }
                    if (accepted) {
                        originalTitle = snapshot["title"]
                        originalActiveId = snapshot["activeId"]
                        originalMode = snapshot[command]
                        submittedAt = SystemClock.elapsedRealtime()
                        handler.postDelayed(this, 200)
                        return
                    }
                    // 從最近使用畫面移除後，Samsung 可能保留 STOPPED session。
                    // 這條連線不代表播放器已載入；必須透過公開播放按鍵重新初始化。
                    val stopped = snapshot["playbackState"] in listOf(0, 1)
                    if (!wokePlayer && (snapshot["connected"] != true || stopped)) {
                        wokePlayer = true
                        if (song == null && command != "resume" && !stopped) {
                            if (serviceConnection != null) {
                                handler.postDelayed(this, 200)
                                return
                            }
                            // 只綁定對外公開的媒體服務以維持生命週期；不呼叫私有 Binder，
                            // 不讀取媒體庫，也不要求 PLAY，切歌與模式調整不意外開始播歌。
                            val service = Intent("android.media.browse.MediaBrowserService").setPackage(Bridge.PACKAGE)
                            @Suppress("DEPRECATION")
                            val entry = app.packageManager.queryIntentServices(service, 0)
                                .firstOrNull { it.serviceInfo.exported && it.serviceInfo.permission.isNullOrEmpty() }
                            if (entry == null) { finish(app.getString(R.string.background_missing)); return }
                            val connection = object : ServiceConnection {
                                override fun onServiceConnected(name: ComponentName, binder: IBinder) { }
                                override fun onServiceDisconnected(name: ComponentName) {
                                    // 對方被關閉後，舊綁定不能當作仍存活的服務使用。
                                    runCatching { app.unbindService(this) }
                                    if (serviceConnection === this) serviceConnection = null
                                }
                            }
                            if (!app.bindService(service.setClassName(entry.serviceInfo.packageName, entry.serviceInfo.name),
                                    connection, Context.BIND_AUTO_CREATE)) {
                                finish(app.getString(R.string.background_missing)); return
                            }
                            serviceConnection = connection
                            serviceBoundAt = SystemClock.elapsedRealtime()
                            handler.postDelayed(this, 200)
                            return
                        }
                        val query = Intent(Intent.ACTION_MEDIA_BUTTON).setPackage(Bridge.PACKAGE)
                        // 限定 package 與明確接收器，避免把 PLAY 送給 YouTube 等其他播放器。
                        @Suppress("DEPRECATION")
                        val receiver = app.packageManager.queryBroadcastReceivers(query, 0)
                            .firstOrNull { it.activityInfo.exported && it.activityInfo.permission.isNullOrEmpty() }
                        if (receiver == null) { finish(app.getString(R.string.background_missing)); return }
                        for (action in listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP)) {
                            app.sendBroadcast(Intent(query)
                                .setClassName(receiver.activityInfo.packageName, receiver.activityInfo.name)
                                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                                .putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY)))
                        }
                    }
                    handler.postDelayed(this, 200)
                } catch (e: Exception) { finish(e.message ?: app.getString(R.string.background_error)) }
            }
        }
        pending = attempt
        // Start immediately while handling the user-initiated widget broadcast.
        attempt.run()
    }
}
