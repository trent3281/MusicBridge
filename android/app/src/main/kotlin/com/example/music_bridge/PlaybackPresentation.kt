package com.example.music_bridge

/** 短暫連線期間只保留顯示資料；操作能力與播放狀態仍取自即時 session。 */
object PlaybackPresentation {
    fun merge(live: Map<String, Any?>, saved: Map<String, Any?>?, connecting: Boolean): Map<String, Any?> {
        if (!connecting || saved == null) return live + ("connecting" to connecting)
        val display = live.toMutableMap()
        display["connecting"] = true
        if ((live["queue"] as? List<*>)?.isNotEmpty() != true &&
                (saved["queue"] as? List<*>)?.isNotEmpty() == true) {
            display["queue"] = saved["queue"]
            display["cached"] = true
            // 舊 queue ID 不能對應新 session 的 activeId。
            display["activeId"] = -1L
        }
        if ((live["title"] as? String).isNullOrBlank()) {
            display["title"] = saved["title"]
            display["artist"] = saved["artist"]
        }
        for (key in listOf("repeat", "shuffle")) {
            if (((live[key] as? Number)?.toInt() ?: -1) < 0) display[key] = saved[key]
        }
        return display
    }
}
