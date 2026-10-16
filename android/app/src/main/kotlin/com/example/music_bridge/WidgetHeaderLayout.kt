package com.example.music_bridge

/** 窄版同步縮小正方形封面與右側兩列，維持三區上下對齊。 */
data class WidgetHeaderLayout(val cover: Int, val gap: Int, val buttonPadding: Int,
    val height: Int, val buttonHeight: Int) {
    companion object {
        fun forWidth(width: Int): WidgetHeaderLayout {
            val compact = width < 340
            val height = if (compact) 76 else 88
            val cover = if (width >= 260) height else 0
            val gap = if (cover == 0) 0 else if (compact) 8 else 12
            return WidgetHeaderLayout(cover, gap, if (compact) 4 else 8,
                height, if (compact) 40 else 48)
        }
    }
}
