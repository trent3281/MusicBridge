package com.example.music_bridge

/** 穩定歌曲身分，與會隨媒體工作階段改變的 queueId 分開。 */
data class SongSelection(val mediaId: String?, val uri: String?, val title: String, val artist: String) {
    fun resolve(queue: List<SongSelection>): Int? {
        fun unique(predicate: (SongSelection) -> Boolean): Int? =
            queue.indices.filter { predicate(queue[it]) }.singleOrNull()
        // 有穩定 ID 就只比對該 ID；找不到不能退回同名歌曲而誤播別的版本。
        // 舊版快取沒有 ID 時，才要求歌名與演出者「唯一」相符。
        if (!mediaId.isNullOrBlank()) return unique { it.mediaId == mediaId }
        if (!uri.isNullOrBlank()) return unique { it.uri == uri }
        if (title.isBlank()) return null
        return unique { it.title == title && it.artist == artist }
    }
}
