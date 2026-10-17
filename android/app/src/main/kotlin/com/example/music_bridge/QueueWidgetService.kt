package com.example.music_bridge

import android.content.Intent
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

/**
 * 完整佇列保留在本程序；跨程序只回傳 Launcher 要求的歌曲列。
 * 不傳送 MediaSession 舊 queue ID 作為點歌依據，仍使用穩定歌曲身分。
 */
data class QueueWidgetSnapshot(
    val songs: List<Map<String, Any?>> = emptyList(),
    val activeId: Long? = null,
    val foreground: Int = Color.WHITE,
    val secondary: Int = Color.LTGRAY,
    val accent: Int = 0xFFAACDFF.toInt()
)

object QueueWidgetData {
    // 主執行緒發布，Factory 在 Binder 執行緒讀取；一次替換完整快照，
    // 避免更新期間同時看到不同版本的歌曲與播放標記。
    @Volatile var current = QueueWidgetSnapshot()
        private set

    fun publish(state: Map<String, Any?>, foreground: Int, secondary: Int, accent: Int): Boolean {
        @Suppress("UNCHECKED_CAST")
        val songs = state["queue"] as? List<Map<String, Any?>> ?: emptyList()
        val next = QueueWidgetSnapshot(songs.map { it.toMap() },
            (state["activeId"] as? Number)?.toLong(), foreground, secondary, accent)
        if (next == current) return false
        current = next
        return true
    }

    fun initialize(context: Context) {
        val mode = context.getSharedPreferences("widget", Context.MODE_PRIVATE).getString("mode", "dark")
        val dark = mode == "dark" || (mode == "system" &&
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        publish(Bridge.snapshot(), if (dark) Color.WHITE else Color.rgb(25, 25, 28),
            if (dark) Color.rgb(205, 205, 210) else Color.rgb(36, 36, 40),
            if (dark) Color.rgb(170, 205, 255) else Color.rgb(36, 92, 180))
    }
}

class QueueWidgetService : RemoteViewsService() {
    override fun onCreate() {
        super.onCreate()
        // 程序被回收後，也能從播放快取重建清單，不要求使用者先開 App。
        Bridge.init(applicationContext)
        // 只準備資料，不回頭更新 Widget；否則服務重建會再次觸發清單刷新。
        QueueWidgetData.initialize(applicationContext)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = QueueFactory()

    private inner class QueueFactory : RemoteViewsFactory {
        private var snapshot = QueueWidgetSnapshot()

        override fun onCreate() { snapshot = QueueWidgetData.current }
        override fun onDataSetChanged() { snapshot = QueueWidgetData.current }
        override fun onDestroy() { snapshot = QueueWidgetSnapshot() }
        override fun getCount(): Int = snapshot.songs.size
        override fun getViewTypeCount(): Int = 1
        // 不假設第三方播放器一定提供唯一且跨連線不變的 queue ID。
        override fun hasStableIds(): Boolean = false
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getLoadingView(): RemoteViews = RemoteViews(packageName, R.layout.queue_row)

        override fun getViewAt(position: Int): RemoteViews? {
            val state = snapshot
            val item = state.songs.getOrNull(position) ?: return null
            val active = (item["id"] as? Number)?.toLong() == state.activeId
            return RemoteViews(packageName, R.layout.queue_row).apply {
                // 使用向量資源與色彩濾鏡，避免每列傳送重複的 Bitmap。
                setImageViewResource(R.id.row_indicator, R.drawable.ic_queue_play)
                setInt(R.id.row_indicator, "setColorFilter", state.accent)
                setViewVisibility(R.id.row_indicator, if (active) View.VISIBLE else View.INVISIBLE)
                setTextViewText(R.id.row_title, item["title"] as? String ?: getString(R.string.unknown_song))
                setTextViewText(R.id.row_artist, item["artist"] as? String ?: "")
                setTextColor(R.id.row_title, if (active) state.accent else state.foreground)
                setTextColor(R.id.row_artist, if (active) state.accent else state.secondary)
                setTextViewTextSize(R.id.row_title, android.util.TypedValue.COMPLEX_UNIT_SP, 17f * .9f)
                setTextViewTextSize(R.id.row_artist, android.util.TypedValue.COMPLEX_UNIT_SP, 14f * .9f)
                setOnClickFillInIntent(R.id.row, Intent()
                    .putExtra("mediaId", item["mediaId"] as? String)
                    .putExtra("uri", item["uri"] as? String)
                    .putExtra("title", item["title"] as? String)
                    .putExtra("artist", item["artist"] as? String))
            }
        }
    }
}
