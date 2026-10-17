package com.example.music_bridge

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 可單元測試的純快取邏輯，不操作 Android 儲存 API。
 * 只保留顯示資料，不把舊 session token、動作旗標或 queue ID 當作操作授權。
 */
class PlaybackMemory(initial: Map<String, Any?> = emptyMap()) {
    var saved: Map<String, Any?> = initial
        private set

    fun record(live: Map<String, Any?>, now: Long): Boolean {
        val queue = live["queue"] as? List<*> ?: return false
        if (live["connected"] != true || live["cached"] == true || queue.isEmpty()) return false
        val content = listOf("title", "artist", "queue", "repeat", "shuffle")
            .associateWith { key ->
                val value = live[key]
                when {
                    value == null -> saved[key]
                    value is String && value.isBlank() -> saved[key]
                    value is Number && value.toInt() < 0 && key in listOf("repeat", "shuffle") -> saved[key]
                    else -> value
                }
            }
        if (content.all { (key, value) -> saved[key] == value }) return false
        saved = content + ("cachedAt" to now)
        return true
    }

    fun offline(permission: Boolean): Map<String, Any?>? {
        // 保留歌名與清單，但播放中狀態、目前項目與操作能力一律清除。
        if ((saved["queue"] as? List<*>)?.isNotEmpty() != true) return null
        return saved + mapOf(
            "connected" to false, "cached" to true, "permission" to permission,
            "playing" to false, "actions" to 0L, "activeId" to -1L,
            "repeat" to ((saved["repeat"] as? Number)?.toInt() ?: -1),
            "shuffle" to ((saved["shuffle"] as? Number)?.toInt() ?: -1),
            "message" to if (permission) "Samsung Music 暫時離線，顯示上次清單"
                else "通知存取權未開啟；保留上次清單，重新授權後恢復同步"
        )
    }
}

/** 將 PlaybackMemory 的結果轉成 JSON，存入 App 私有 SharedPreferences。 */
object PlaybackCache {
    private var initialized = false
    private var memory = PlaybackMemory()
    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val text = context.getSharedPreferences("playback_cache", Context.MODE_PRIVATE).getString("snapshot", null)
        memory = PlaybackMemory(runCatching {
            if (text == null) emptyMap() else decode(JSONObject(text))
        }.getOrDefault(emptyMap()))
    }
    fun record(context: Context, live: Map<String, Any?>) {
        if (memory.record(live, System.currentTimeMillis())) {
            context.getSharedPreferences("playback_cache", Context.MODE_PRIVATE).edit()
                .putString("snapshot", JSONObject(memory.saved).toString()).apply()
        }
    }
    fun offline(permission: Boolean) = memory.offline(permission)
    private fun decode(json: JSONObject): Map<String, Any?> = json.keys().asSequence().associateWith { key ->
        fun value(raw: Any?): Any? = when (raw) {
            JSONObject.NULL -> null
            is JSONObject -> decode(raw)
            is JSONArray -> (0 until raw.length()).map { value(raw.get(it)) }
            // Queue IDs must stay Long across JSON and Flutter method-channel round trips.
            is Int -> raw.toLong()
            else -> raw
        }
        value(json.get(key))
    }.mapValues { (key, value) ->
        if (key == "repeat" || key == "shuffle") (value as? Number)?.toInt() ?: -1 else value
    }
}
