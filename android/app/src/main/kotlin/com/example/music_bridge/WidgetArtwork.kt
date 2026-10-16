package com.example.music_bridge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** 缺少封面時永遠產生同一種佔位圖，不讓 ImageView 放大裸音符資源。 */
object WidgetArtwork {
    fun render(context: Context, source: Bitmap?, foreground: Int): Bitmap {
        // 封面始終為正方形；窄版由版面同步縮小左右兩區，不拉伸圖片。
        val width = 256
        val bitmap = Bitmap.createBitmap(width, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(0f, 0f, width.toFloat(), 256f)
        val path = android.graphics.Path().apply {
            addRoundRect(bounds, 24f, 24f, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)
        if (source != null) {
            val scale = maxOf(width.toFloat() / source.width, 256f / source.height)
            val w = source.width * scale
            val h = source.height * scale
            canvas.drawBitmap(source, null,
                RectF((width-w)/2, (256-h)/2, (width+w)/2, (256+h)/2),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            return bitmap
        }
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, Color.red(foreground), Color.green(foreground), Color.blue(foreground))
        }
        canvas.drawRect(bounds, background)
        context.getDrawable(R.drawable.ic_music)!!.mutate().apply {
            setTint(foreground)
            val extent = (minOf(width, 256) * .375f).toInt()
            setBounds((width-extent)/2, (256-extent)/2, (width+extent)/2, (256+extent)/2)
            draw(canvas)
        }
        return bitmap
    }
}
