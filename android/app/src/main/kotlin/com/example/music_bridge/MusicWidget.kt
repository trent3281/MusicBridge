package com.example.music_bridge

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** 桌面小工具的系統事件入口：新增、縮放、更新與按鈕廣播。 */
class MusicWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        Bridge.init(context); Bridge.connect(); updateAll(context)
    }
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) {
        updateAll(context)
    }
    override fun onReceive(context: Context, intent: Intent) {
        // 空白處點擊只由本接收器消耗，不連線、不重畫、不啟動 Activity。
        if (intent.action == "com.example.music_bridge.NO_OP") return
        super.onReceive(context, intent)
        if (intent.action == "com.example.music_bridge.SELECT") {
            // 等待播放器恢復時暫時保留廣播，完成或取消都必須 finish。
            // 不建立 Activity，點歌才不會閃出視窗。
            val result = goAsync()
            BackgroundPlayback.select(context, SongSelection(intent.getStringExtra("mediaId"),
                intent.getStringExtra("uri"), intent.getStringExtra("title").orEmpty(),
                intent.getStringExtra("artist").orEmpty())) { result.finish() }
            return
        }
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Bridge.init(context)
            Bridge.connect()
            updateAll(context)
        }
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED || intent.action == Intent.ACTION_LOCALE_CHANGED) updateAll(context)
        if (intent.action == "com.example.music_bridge.CONTROL") {
            Bridge.init(context)
            try { Bridge.command(intent.getStringExtra("command") ?: "", if (intent.hasExtra("id")) intent.getLongExtra("id", -1) else null) }
            catch (e: Exception) { Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
            updateAll(context)
        }
    }
    companion object {
        /** 保留舊呼叫入口，繪製細節交給專責類別。 */
        fun updateAll(context: Context) = MusicWidgetRenderer.updateAll(context)
    }
}
