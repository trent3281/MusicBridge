package com.example.music_bridge

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.content.res.Configuration
import android.widget.RemoteViews

/**
 * 專責建立 RemoteViews：Launcher 接收這種受限制的畫面描述，不能直接用
 * Flutter 的 Container / Row。真正桌面版面在 res/layout 目錄的 XML 檔案。
 * 此處處理文字、圖示、點擊與背景；系統廣播入口留在 MusicWidget。
 */
object MusicWidgetRenderer {
    fun updateAll(context: Context) {
        Bridge.init(context)
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, MusicWidget::class.java))
        if (ids.isEmpty()) return
        val state = Bridge.snapshot()
        val connected = state["connected"] == true
        val actions = state["actions"] as Long
        val playing = state["playing"] == true
        val repeat = state["repeat"] as Int
        val views = RemoteViews(context.packageName, R.layout.music_widget)
        views.setTextViewText(R.id.title, state["title"] as String? ?: "Samsung Music")
        views.setTextViewText(R.id.artist, state["artist"] as String? ?: "開啟 Samsung Music 並播放歌曲")
        val prefs = context.getSharedPreferences("widget", Context.MODE_PRIVATE)
        // 設定由 Flutter 儲存到原生；Widget 不必啟動 Flutter 引擎就能讀取。
        val mode = prefs.getString("mode", "dark")
        val dark = mode == "dark" || (mode == "system" &&
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        val foreground = if (dark) Color.WHITE else Color.rgb(25, 25, 28)
        val secondary = if (dark) Color.rgb(205, 205, 210) else Color.rgb(36, 36, 40)
        val opacity = prefs.getFloat("opacity", .35f).coerceIn(0f, 1f)
        val fontScale = .9f
        views.setTextViewTextSize(R.id.title, android.util.TypedValue.COMPLEX_UNIT_SP, 18f * fontScale)
        views.setTextViewTextSize(R.id.artist, android.util.TypedValue.COMPLEX_UNIT_SP, 14f * fontScale)
        views.setTextColor(R.id.title, foreground)
        views.setTextColor(R.id.artist, secondary)
        views.setTextColor(R.id.empty, secondary)
        val songCount = (state["queue"] as? List<*>)?.size ?: 0
        views.setTextViewText(R.id.queue_count, "$songCount 首")
        views.setTextColor(R.id.queue_count, secondary)
        views.setContentDescription(R.id.queue_count,
            if (state["cached"] == true) "上次播放清單，共 $songCount 首" else "目前播放清單，共 $songCount 首")
        // 圖示先畫成帶顏色的 Bitmap，避免不同 Launcher 對 tint 支援不一致。
        fun icon(id: Int, drawable: Int, color: Int = foreground) {
            val asset = context.getDrawable(drawable)!!.mutate()
            asset.setTint(color)
            val bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
            asset.setBounds(0, 0, 72, 72)
            val canvas = Canvas(bitmap)
            // Control glyphs: 76% of original size (20% smaller than the previous 95%).
            if (id != R.id.music_icon) canvas.scale(.76f, .76f, 36f, 36f)
            asset.draw(canvas)
            views.setImageViewBitmap(id, bitmap)
        }
        icon(R.id.music_icon, R.drawable.ic_music)
        Artwork.bitmap?.let { views.setImageViewBitmap(R.id.music_icon, it) }
        icon(R.id.previous, R.drawable.ic_previous)
        icon(R.id.next, R.drawable.ic_next)
        icon(R.id.toggle, if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        val accent = if (dark) Color.rgb(170, 205, 255) else Color.rgb(36, 92, 180)
        val shuffle = state["shuffle"] as Int
        icon(R.id.shuffle, if (shuffle == 0) R.drawable.ic_shuffle_off else R.drawable.ic_shuffle)
        views.setContentDescription(R.id.shuffle, when (shuffle) {
            0 -> "隨機播放關閉"
            1, 2 -> "隨機播放開啟"
            else -> "隨機播放狀態未公開"
        })
        icon(R.id.repeat, when (repeat) {
            1 -> R.drawable.ic_repeat_one
            2, 3 -> R.drawable.ic_repeat_all
            0 -> R.drawable.ic_play_once
            else -> R.drawable.ic_repeat
        })
        views.setContentDescription(R.id.repeat, when (repeat) { 1 -> "單曲循環"; 2, 3 -> "全部循環"; 0 -> "全部播放一次"; else -> "循環模式未公開" })
        views.setContentDescription(R.id.toggle, if (playing) "暫停" else "播放")
        val launch = context.packageManager.getLaunchIntentForPackage(Bridge.PACKAGE)
        // 標題區明確開啟播放器；與下面「歌曲列背景點歌」是不同操作。
            ?: Intent(context, MainActivity::class.java)
        val open = PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.header, open)
        listOf(Triple(R.id.shuffle, "shuffle", 2097152L), Triple(R.id.previous, "previous", 16L), Triple(R.id.next, "next", 32L),
            Triple(R.id.toggle, "toggle", (if (playing) 2L else 4L) or 512L),
            Triple(R.id.repeat, "repeat", 262144L)).forEach { (view, command, flag) ->
            val enabled = actions and flag != 0L || (!connected && command == "toggle")
            views.setBoolean(view, "setEnabled", enabled)
            views.setFloat(view, "setAlpha", if (enabled) 1f else .3f)
            val intent = Intent(context, MusicWidget::class.java).setAction("com.example.music_bridge.CONTROL").putExtra("command", command)
            views.setOnClickPendingIntent(view, if (!connected && command == "toggle") open
                else PendingIntent.getBroadcast(context, view, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
        // 完整佇列留在 App 程序，Launcher 透過服務取得歌曲列。
        QueueWidgetData.publish(state, foreground, secondary, accent)
        val template = Intent(context, MusicWidget::class.java).setAction("com.example.music_bridge.SELECT")
        // 範本固定送給自己的廣播接收器；每列 fill-in Intent 補上那一首的身分。
        // MUTABLE 是讓系統能填入列資料，並非讓點歌去啟動 Activity。
        views.setPendingIntentTemplate(R.id.queue,
            PendingIntent.getBroadcast(context, 103, template, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))
        views.setEmptyView(R.id.queue, R.id.empty)
        views.setTextViewText(R.id.empty, when {
            state["permission"] != true -> "請開啟通知存取權以同步歌曲"
            connected -> "Samsung Music 未公開播放佇列"
            else -> "尚無已儲存清單；在 Samsung Music 播放一次即可"
        })
        views.setContentDescription(R.id.header, if (state["cached"] == true)
            "上次播放清單；點擊開啟 Samsung Music" else "開啟 Samsung Music")
        if (!connected) views.setContentDescription(R.id.toggle, "開啟 Samsung Music")
        ids.forEach { id ->
            // 每個 Widget 尺寸可能不同；依 Launcher 提供的尺寸畫圓角透明背景。
            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).coerceIn(180, 600)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 380).coerceIn(110, 1000)
            val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val color = if (dark) 24 else 248
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.argb((opacity * 255).toInt(), color, color, color)
            }
            Canvas(background).drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 28f, 28f, paint)
            views.setImageViewBitmap(R.id.background, background)
            val adapter = Intent(context, QueueWidgetService::class.java).apply {
                data = android.net.Uri.parse("musicbridge://queue/$id")
            }
            views.setRemoteAdapter(R.id.queue, adapter)
            manager.updateAppWidget(id, views)
            manager.notifyAppWidgetViewDataChanged(id, R.id.queue)
        }
    }
}
