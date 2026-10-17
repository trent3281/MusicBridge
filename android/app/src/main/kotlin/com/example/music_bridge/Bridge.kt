package com.example.music_bridge

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

/**
 * 原生播放協調器：尋找 Samsung Music 的 MediaSession、訂閱回呼並提供快照。
 * Flutter 經 MainActivity 呼叫這裡；桌面 Widget 也使用同一份播放資料。
 * App 關閉後仍由 MusicListener 監聽，不能只靠 Flutter Timer 同步。
 */
object Bridge {
    const val PACKAGE = "com.sec.android.app.music"
    private lateinit var context: Context
    private var controller: MediaControllerCompat? = null
    private val handler = Handler(Looper.getMainLooper())
    private var watching = false
    private var message = ""
    private var observation = ShuffleObservation()
    private var observedQueue: List<MediaSessionCompat.QueueItem> = emptyList()
    private var observedMode = -1
    private var observedRepeat = -1
    private var visualTracker = PlaybackVisualTracker()
    private var lastProbe = ""
    private val history = java.util.ArrayDeque<String>()
    private var monitoring = false
    private var sessionConnectedAt = 0L
    private val transitionExpired = Runnable { changed() }
    private val poll = object : Runnable {
        override fun run() {
            if (permission()) connect()
            probe("定時讀取")
            if (monitoring) handler.postDelayed(this, 2000)
        }
    }
    private val delayedProbes = mutableListOf<Runnable>()
    fun startMonitoring() {
        monitoring = true
        handler.removeCallbacks(poll)
        handler.post(poll)
    }
    // 所有回呼與補查共用比較入口；以播放器回報為準，不先猜測指令成功。
    private fun probe(source: String, suppliedQueue: List<MediaSessionCompat.QueueItem>? = null,
        suppliedExtras: android.os.Bundle? = null, repeatOverride: Int? = null, shuffleOverride: Int? = null) {
        val c = controller ?: return
        val queue = suppliedQueue ?: c.queue.orEmpty()
        val extras = c.extras
        fun extraMode(bundle: android.os.Bundle?, key: String): Int? {
            @Suppress("DEPRECATION")
            return (bundle?.get(key) as? Number)?.toInt()?.takeIf { it >= 0 }
        }
        val mode = shuffleOverride
            ?: extraMode(suppliedExtras, "com.sec.android.app.music.EXTRA_SHUFFLE")
            ?: extraMode(extras, "com.sec.android.app.music.EXTRA_SHUFFLE") ?: c.shuffleMode
        val repeat = repeatOverride
            ?: extraMode(suppliedExtras, "com.sec.android.app.music.EXTRA_REPEAT")
            ?: extraMode(extras, "com.sec.android.app.music.EXTRA_REPEAT") ?: c.repeatMode
        val playback = c.playbackState
        val visualChanged = visualTracker.observe(PlaybackVisualState(repeat, mode,
            playback?.activeQueueItemId ?: -1L, playback?.state ?: 0, playback?.actions ?: 0L))
        val oldStatus = observation.status
        val orderChanged = observation.observe(mode, queue.map { it.queueId }, android.os.SystemClock.elapsedRealtime())
        val modeChanged = mode != observedMode || repeat != observedRepeat
        observedQueue = queue.toList()
        observedMode = mode
        observedRepeat = repeat
        lastProbe = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.ROOT).format(java.util.Date())
        if (orderChanged || modeChanged || source != "定時讀取") {
            val ids = queue.take(8).joinToString(",") { it.queueId.toString() }
            history.addLast("$lastProbe $source：隨機=$mode，循環=$repeat，清單#${observation.queueRevision}，${queue.size}首，前8 ID=[$ids]")
            while (history.size > 16) history.removeFirst()
        }
        if (orderChanged || visualChanged || oldStatus != observation.status) changed()
        if (orderChanged || visualChanged || oldStatus != observation.status || source != "定時讀取") {
            runCatching { java.io.File(context.filesDir, "media-diagnostics.json")
                .writeText(org.json.JSONObject(snapshot()).toString(2)) }
        }
    }
    private fun followUp(source: String) {
        delayedProbes.forEach { handler.removeCallbacks(it) }
        delayedProbes.clear()
        listOf(200L, 700L, 1500L, 3200L, 5500L).forEach { delay ->
            val task = Runnable { probe(source) }
            delayedProbes.add(task)
            handler.postDelayed(task, delay)
        }
    }
    fun inspect() {
        connect()
        probe("手動比對")
        followUp("手動比對後讀取")
        runCatching { java.io.File(context.filesDir, "media-diagnostics.json")
            .writeText(org.json.JSONObject(snapshot()).toString(2)) }
    }
    fun onPlayerNotificationChanged() {
        connect()
        probe("Samsung Music 通知更新")
        followUp("通知更新後讀取")
    }
    private val callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Artwork.load(context, metadata)
            probe("歌曲變更")
            changed()
        }
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) { probe("播放狀態回報"); changed() }
        override fun onExtrasChanged(extras: android.os.Bundle?) { probe("額外狀態變更", suppliedExtras = extras) }
        override fun onSessionEvent(event: String?, extras: android.os.Bundle?) {
            probe("播放器事件：${event.orEmpty().take(120)}")
            followUp("播放器事件後讀取")
        }
        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            probe("佇列回報", queue.orEmpty())
        }
        override fun onRepeatModeChanged(repeatMode: Int) {
            probe("循環模式回報=$repeatMode", repeatOverride = repeatMode)
            followUp("循環變更後讀取")
        }
        override fun onShuffleModeChanged(shuffleMode: Int) {
            probe("隨機模式回報=$shuffleMode", shuffleOverride = shuffleMode)
            changed()
            followUp("模式變更後讀取")
        }
        override fun onSessionReady() { probe("媒體連線就緒"); changed() }
        override fun onSessionDestroyed() { detach(); changed(); handler.post { connect() } }
    }
    private val listener = MediaSessionManager.OnActiveSessionsChangedListener { connect() }
    fun init(ctx: Context) {
        context = ctx.applicationContext
        PlaybackCache.init(context)
    }
    fun permission(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
        val component = ComponentName(context, MusicListener::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
    }
    fun connect() {
        val manager = context.getSystemService(MediaSessionManager::class.java)
        if (!permission()) { message = "請開啟通知存取權"; stop(); return }
        MusicListener.ensureConnected(context)
        try {
            if (!watching) {
                manager.addOnActiveSessionsChangedListener(listener, ComponentName(context, MusicListener::class.java), handler)
                watching = true
            }
            val session = manager.getActiveSessions(ComponentName(context, MusicListener::class.java))
                .filter { it.packageName == PACKAGE }
                .sortedByDescending { it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING }
                .firstOrNull()
            val token = session?.sessionToken?.let { MediaSessionCompat.Token.fromToken(it) }
            if (controller?.sessionToken != token) {
                detach()
                if (token != null) {
                    sessionConnectedAt = android.os.SystemClock.elapsedRealtime()
                    controller = MediaControllerCompat(context, token)
                    controller?.registerCallback(callback, handler)
                    Artwork.load(context, controller?.metadata)
                    probe("新媒體工作階段")
                    // 若播放器真的沒有佇列，過渡期結束後仍要顯示真實空清單。
                    handler.postDelayed(transitionExpired, 3000)
                }
                changed()
            }
            message = if (controller == null) "請開啟 Samsung Music 並播放歌曲" else "資料由 Samsung Music MediaSession 提供"
        } catch (e: SecurityException) { stop(); message = "無法讀取媒體工作階段：請重新授權通知存取權" }
    }
    // 先存最後有效清單，再清掉連線狀態；快取不是操作權或 session token。
    private fun detach() {
        runCatching { PlaybackCache.record(context, snapshot()) }
        handler.removeCallbacks(transitionExpired)
        sessionConnectedAt = 0L
        delayedProbes.forEach { handler.removeCallbacks(it) }
        delayedProbes.clear()
        observedQueue = emptyList()
        observedMode = -1
        observedRepeat = -1
        visualTracker = PlaybackVisualTracker()
        observation = ShuffleObservation()
        history.clear()
        controller?.unregisterCallback(callback)
        controller = null
        Artwork.load(context, null)
    }
    fun stop() {
        monitoring = false
        handler.removeCallbacks(poll)
        if (watching) {
            context.getSystemService(MediaSessionManager::class.java).removeOnActiveSessionsChangedListener(listener)
            watching = false
        }
        detach()
        changed()
    }
    private fun changed() {
        PlaybackCache.record(context, snapshot())
        MusicWidget.updateAll(context)
    }
    // 只回傳 MethodChannel 可傳遞的簡單型別，Dart 在 PlaybackSnapshot 解析。
    fun snapshot(): Map<String, Any?> {
        val c = controller
        val state = c?.playbackState
        val metadata = c?.metadata
        val live = mapOf(
            "permission" to permission(), "connected" to (c != null),
            "cached" to false,
            "artworkRevision" to Artwork.revision,
            "title" to metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
            "artist" to metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
            "playing" to (state?.state == PlaybackStateCompat.STATE_PLAYING),
            "playbackState" to (state?.state ?: PlaybackStateCompat.STATE_NONE),
            "actions" to (state?.actions ?: 0L),
            "activeId" to (state?.activeQueueItemId ?: -1L),
            "repeat" to observedRepeat,
            "shuffle" to observedMode,
            "shuffleStatus" to observation.status,
            "queueRevision" to observation.queueRevision,
            "lastProbe" to lastProbe,
            "sessionReady" to (c?.isSessionReady ?: false),
            "mediaEvents" to history.toList(),
            "customActions" to (state?.customActions?.map { "${it.action}: ${it.name}" } ?: emptyList<String>()),
            "customActionDetails" to (state?.customActions?.map {
                mapOf("action" to it.action, "name" to it.name.toString(), "extras" to bundleDetails(it.extras))
            } ?: emptyList<Map<String, Any?>>()),
            "playbackExtras" to bundleDetails(state?.extras),
            "sessionExtras" to bundleDetails(c?.extras),
            "message" to if (!permission()) "請開啟通知存取權"
                else if (c == null) "Samsung Music 尚未啟動" else message,
            "queue" to (observedQueue.map {
                mapOf("id" to it.queueId, "title" to it.description.title?.toString(),
                    "mediaId" to it.description.mediaId, "uri" to it.description.mediaUri?.toString(),
                    "artist" to it.description.subtitle?.toString())
            })
        )
        val saved = PlaybackCache.offline(permission())
        if (c == null) return live + (saved ?: emptyMap())
        val connecting = android.os.SystemClock.elapsedRealtime() - sessionConnectedAt < 3000 &&
            (observedQueue.isEmpty() || state == null || !c.isSessionReady ||
                metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE).isNullOrBlank())
        return PlaybackPresentation.merge(live, saved, connecting)
    }
    private fun bundleDetails(bundle: android.os.Bundle?): Map<String, String> =
        runCatching { bundle?.keySet()?.associateWith { key ->
            @Suppress("DEPRECATION")
            bundle.get(key).toString().take(1000)
        }.orEmpty() }.getOrDefault(emptyMap())
    // Resolve against the current session only. Cached queue IDs are session-specific.
    fun playSelection(selection: SongSelection): Boolean {
        connect()
        val c = controller ?: return false
        val queue = c.queue.orEmpty()
        val index = selection.resolve(queue.map { SongSelection(it.description.mediaId,
            it.description.mediaUri?.toString(), it.description.title?.toString().orEmpty(),
            it.description.subtitle?.toString().orEmpty()) }) ?: return false
        val actions = c.playbackState?.actions ?: 0L
        if (actions and PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM == 0L) return false
        val id = queue[index].queueId
        c.transportControls.skipToQueueItem(id)
        c.transportControls.play()
        followUp("重新連線點歌")
        return true
    }
    /** 等待背景恢復媒體連線後，從播放器自己保留的目前位置繼續播放。 */
    fun resumePlayback(): Boolean {
        connect()
        val c = controller ?: return false
        val state = c.playbackState ?: return false
        if (state.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) return false
        // 媒體按鍵可能已經成功開始播放，不再次切換為暫停。
        if (state.state == PlaybackStateCompat.STATE_PLAYING) return true
        if (state.actions and (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE) == 0L) return false
        c.transportControls.play()
        followUp("背景恢復播放")
        return true
    }

    /** 背景服務尚未完成初始化時先等候，連線就緒後才驗證及送出一次操作。 */
    fun backgroundCommand(action: String): Boolean {
        connect()
        val c = controller ?: return false
        if (!c.isSessionReady || c.playbackState == null) return false
        if (c.playbackState?.state in listOf(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE)) return false
        // 暫停且剛恢復的 Samsung session 可能接受 skip 指令卻沒有切歌。
        // 用即時佇列定位目標，沿用清單點歌使用的公開 queue-item 操作；不要求播放。
        if (action in listOf("previous", "next") &&
            (if (observedMode >= 0) observedMode else c.shuffleMode) == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            val queue = c.queue.orEmpty()
            val index = queue.indexOfFirst { it.queueId == c.playbackState?.activeQueueItemId }
            if (index < 0 || queue.isEmpty()) return false
            val target = Math.floorMod(index + if (action == "next") 1 else -1, queue.size)
            c.transportControls.skipToQueueItem(queue[target].queueId)
            followUp("背景佇列切歌：$action")
            return true
        }
        command(action)
        followUp("背景控制：$action")
        return true
    }

    fun command(action: String, id: Long? = null) {
        if (action == "inspect") { inspect(); return }
        connect()
        val c = controller ?: error(context.getString(R.string.not_connected))
        val controls = c.transportControls
        val actions = c.playbackState?.actions ?: 0L
        fun requireAction(flag: Long) { check(actions and flag != 0L) { context.getString(R.string.unsupported_action) } }
        when (action) {
            "shuffle" -> {
                requireAction(PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                check(c.isSessionReady) { context.getString(R.string.session_wait) }
                val current = if (observedMode >= 0) observedMode else c.shuffleMode
                val target = if (current == PlaybackStateCompat.SHUFFLE_MODE_ALL ||
                    current == PlaybackStateCompat.SHUFFLE_MODE_GROUP)
                    PlaybackStateCompat.SHUFFLE_MODE_NONE else PlaybackStateCompat.SHUFFLE_MODE_ALL
                observation.request(target, c.queue.orEmpty().map { it.queueId }, android.os.SystemClock.elapsedRealtime())
                controls.setShuffleMode(target)
                probe("Widget 送出隨機=$target")
                followUp("指令後讀取")
                changed()
            }
            "previous" -> { requireAction(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS); controls.skipToPrevious() }
            "next" -> { requireAction(PlaybackStateCompat.ACTION_SKIP_TO_NEXT); controls.skipToNext() }
            "toggle" -> {
                val playing = c.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
                val flag = if (playing) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
                check(actions and (flag or PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L) { context.getString(R.string.no_controls) }
                if (playing) controls.pause() else controls.play()
            }
            "queue" -> {
                requireAction(PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM)
                check(id != null && c.queue?.any { it.queueId == id } == true) { context.getString(R.string.song_removed) }
                controls.skipToQueueItem(id!!)
            }
            "repeat" -> {
                requireAction(PlaybackStateCompat.ACTION_SET_REPEAT_MODE)
                // Samsung modes: repeat one -> repeat all -> play queue once.
                val next = when (if (observedRepeat >= 0) observedRepeat else c.repeatMode) {
                    PlaybackStateCompat.REPEAT_MODE_ONE -> PlaybackStateCompat.REPEAT_MODE_ALL
                    PlaybackStateCompat.REPEAT_MODE_ALL, PlaybackStateCompat.REPEAT_MODE_GROUP -> PlaybackStateCompat.REPEAT_MODE_NONE
                    else -> PlaybackStateCompat.REPEAT_MODE_ONE
                }
                controls.setRepeatMode(next)
            }
            else -> error("未知操作：$action")
        }
        // No optimistic playback state: Samsung Music callbacks remain the source of truth.
    }
}
