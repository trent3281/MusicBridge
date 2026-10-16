package com.example.music_bridge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

/**
 * 只讀 Samsung Music 當前工作階段提供的專輯圖，不掃描整個手機圖片。
 * 圖片解碼交給背景執行緒，完成後回主執行緒通知 Widget 更新，避免卡住點擊。
 * generation 用來丟棄切歌前尚未解碼完成的舊圖片，revision 供 Flutter 判斷要不要重讀。
 */
object Artwork {
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var generation = 0
    var bitmap: Bitmap? = null
        private set
    var bytes: ByteArray? = null
        private set
    var revision = 0
        private set

    fun load(context: Context, metadata: MediaMetadataCompat?) {
        val request = ++generation
        // Clear immediately when changing tracks, so the previous album never lingers.
        bitmap = null
        bytes = null
        revision++
        if (metadata == null) return
        executor.execute {
            val image = runCatching {
                val embedded = listOf(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    MediaMetadataCompat.METADATA_KEY_ART, MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)
                    .firstNotNullOfOrNull { metadata.getBitmap(it) }
                val source = embedded ?: listOf(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                    MediaMetadataCompat.METADATA_KEY_ART_URI, MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)
                    .firstNotNullOfOrNull { key ->
                        runCatching {
                            val uri = metadata.getString(key)?.let(Uri::parse) ?: return@runCatching null
                            if (uri.scheme !in listOf("content", "file", "android.resource")) return@runCatching null
                            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                            val options = BitmapFactory.Options()
                            while (bounds.outWidth / options.inSampleSize > 512 ||
                                bounds.outHeight / options.inSampleSize > 512) options.inSampleSize *= 2
                            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                        }.getOrNull()
                    }
                source?.let { original ->
                    val output = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    val path = android.graphics.Path().apply {
                        addRoundRect(RectF(0f, 0f, 256f, 256f), 24f, 24f, android.graphics.Path.Direction.CW)
                    }
                    canvas.clipPath(path)
                    val scale = maxOf(256f / original.width, 256f / original.height)
                    val w = original.width * scale
                    val h = original.height * scale
                    canvas.drawBitmap(original, null, RectF((256-w)/2, (256-h)/2, (256+w)/2, (256+h)/2), Paint(Paint.FILTER_BITMAP_FLAG))
                    output
                }
            }.getOrNull()
            val encoded = image?.let { ByteArrayOutputStream().use { out ->
                it.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            } }
            main.post {
                if (request == generation) {
                    bitmap = image
                    bytes = encoded
                    revision++
                    MusicWidget.updateAll(context)
                }
            }
        }
    }
}
