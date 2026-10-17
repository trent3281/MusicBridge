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
        views.setTextViewText(R.id.artist, state["artist"] as String? ?: context.getString(R.string.start_music))
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
        views.setTextViewText(R.id.queue_count, context.resources.getQuantityString(R.plurals.queue_song_count, songCount, songCount))
        views.setTextColor(R.id.queue_count, secondary)
        views.setContentDescription(R.id.queue_count,
            if (state["cached"] == true) context.getString(R.string.cached_count, songCount) else context.getString(R.string.current_count, songCount))
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
        icon(R.id.previous, R.drawable.ic_previous)
        icon(R.id.next, R.drawable.ic_next)
        icon(R.id.toggle, if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        val accent = if (dark) Color.rgb(170, 205, 255) else Color.rgb(36, 92, 180)
        val shuffle = state["shuffle"] as Int
        icon(R.id.shuffle, if (shuffle == 0) R.drawable.ic_shuffle_off else R.drawable.ic_shuffle)
        views.setContentDescription(R.id.shuffle, when (shuffle) {
            0 -> context.getString(R.string.shuffle_off)
            1, 2 -> context.getString(R.string.shuffle_on)
            else -> context.getString(R.string.shuffle_unknown)
        })
        icon(R.id.repeat, when (repeat) {
            1 -> R.drawable.ic_repeat_one
            2, 3 -> R.drawable.ic_repeat_all
            0 -> R.drawable.ic_play_once
            else -> R.drawable.ic_repeat
        })
        views.setContentDescription(R.id.repeat, when (repeat) { 1 -> context.getString(R.string.repeat_one); 2, 3 -> context.getString(R.string.repeat_all); 0 -> context.getString(R.string.play_once); else -> context.getString(R.string.repeat_unknown) })
        views.setContentDescription(R.id.toggle, if (playing) context.getString(R.string.pause) else context.getString(R.string.play))
        val launch = context.packageManager.getLaunchIntentForPackage(Bridge.PACKAGE)
        // 標題區明確開啟播放器；與下面「歌曲列背景點歌」是不同操作。
            ?: Intent(context, MainActivity::class.java)
        val open = PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // 消耗外框空白點擊，避免 Launcher 回退開啟所屬 App；不建立任何畫面。
        // 子項目的播放按鈕與歌曲列仍使用各自的 PendingIntent。
        val idle = Intent(context, MusicWidget::class.java)
            .setAction("com.example.music_bridge.NO_OP")
        views.setOnClickPendingIntent(R.id.widget_root,
            PendingIntent.getBroadcast(context, 104, idle,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.header, open)
        listOf(Triple(R.id.shuffle, "shuffle", 2097152L), Triple(R.id.previous, "previous", 16L), Triple(R.id.next, "next", 32L),
            Triple(R.id.toggle, "toggle", (if (playing) 2L else 4L) or 512L),
            Triple(R.id.repeat, "repeat", 262144L)).forEach { (view, command, flag) ->
            // 離線時允許發出背景連線請求；重新連線後仍以即時動作旗標驗證。
            val enabled = actions and flag != 0L ||
                ((!connected || state["connecting"] == true) && state["permission"] == true)
            views.setBoolean(view, "setEnabled", enabled)
            views.setFloat(view, "setAlpha", if (enabled) 1f else .3f)
            val intent = Intent(context, MusicWidget::class.java).setAction("com.example.music_bridge.CONTROL")
                .putExtra("command", if (!connected && command == "toggle") "resume" else command)
            views.setOnClickPendingIntent(view,
                PendingIntent.getBroadcast(context, view, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
        // 完整佇列留在 App 程序，Launcher 透過服務取得歌曲列。
        val queueChanged = QueueWidgetData.publish(state, foreground, secondary, accent)
        val template = Intent(context, MusicWidget::class.java).setAction("com.example.music_bridge.SELECT")
        // 範本固定送給自己的廣播接收器；每列 fill-in Intent 補上那一首的身分。
        // MUTABLE 是讓系統能填入列資料，並非讓點歌去啟動 Activity。
        views.setPendingIntentTemplate(R.id.queue,
            PendingIntent.getBroadcast(context, 103, template, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))
        views.setEmptyView(R.id.queue, R.id.empty)
        views.setTextViewText(R.id.empty, when {
            state["permission"] != true -> context.getString(R.string.empty_permission)
            connected -> context.getString(R.string.empty_public)
            else -> context.getString(R.string.empty_cache)
        })
        views.setContentDescription(R.id.header, if (state["cached"] == true)
            context.getString(R.string.cached_header) else context.getString(R.string.open_music))
        ids.forEach { id ->
            // 每個 Widget 尺寸可能不同；依 Launcher 提供的尺寸畫圓角透明背景。
            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).coerceIn(180, 600)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 380).coerceIn(110, 1000)
            val header = WidgetHeaderLayout.forWidth(width)
            val widgetViews = views.clone()
            widgetViews.setViewVisibility(R.id.music_icon,
                if (header.cover > 0) android.view.View.VISIBLE else android.view.View.GONE)
            widgetViews.setViewLayoutWidth(R.id.music_icon, header.cover.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            widgetViews.setViewLayoutHeight(R.id.music_icon, header.cover.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            widgetViews.setViewLayoutHeight(R.id.header, header.height.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            widgetViews.setViewLayoutHeight(R.id.header_controls, header.buttonHeight.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            if (header.cover > 0) {
                // 與右側兩列上下對齊；每次明確設定圖片或佔位圖，避免留下舊狀態。
                widgetViews.setImageViewBitmap(R.id.music_icon,
                    WidgetArtwork.render(context, Artwork.bitmap, foreground))
            }
            widgetViews.setViewLayoutMargin(R.id.header_content, RemoteViews.MARGIN_START,
                header.gap.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            val padding = (header.buttonPadding * context.resources.displayMetrics.density).toInt()
            listOf(R.id.shuffle, R.id.previous, R.id.toggle, R.id.next, R.id.repeat).forEach {
                widgetViews.setViewPadding(it, padding, padding, padding, padding)
                widgetViews.setViewLayoutHeight(it, header.buttonHeight.toFloat(), android.util.TypedValue.COMPLEX_UNIT_DIP)
            }
            val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val color = if (dark) 24 else 248
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.argb((opacity * 255).toInt(), color, color, color)
            }
            Canvas(background).drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 28f, 28f, paint)
            widgetViews.setImageViewBitmap(R.id.background, background)
            val adapter = Intent(context, QueueWidgetService::class.java).apply {
                data = android.net.Uri.parse("musicbridge://queue/$id")
            }
            widgetViews.setRemoteAdapter(R.id.queue, adapter)
            manager.updateAppWidget(id, widgetViews)
            // 圖示或連線狀態變更不代表歌曲列變更，避免每次按鍵都清空重載清單。
            if (queueChanged) manager.notifyAppWidgetViewDataChanged(id, R.id.queue)
        }
    }
}
