package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class WidgetHeaderLayoutTest {
    @Test fun narrowWidgetShrinksCoverBeforeControls() {
        assertEquals(88, WidgetHeaderLayout.forWidth(380).cover)
        val narrow = WidgetHeaderLayout.forWidth(280)
        assertEquals(76, narrow.cover)
        assertEquals(narrow.cover, narrow.height)
        assertEquals(40, narrow.buttonHeight)
        assertTrue((280 - 32 - narrow.cover - narrow.gap) / 5 >= 32)
        assertEquals(4, narrow.buttonPadding)
    }

    @Test fun veryNarrowWidgetGivesEntireWidthToControls() {
        for (width in 180..259) {
            val layout = WidgetHeaderLayout.forWidth(width)
            assertEquals(0, layout.cover)
            assertEquals(0, layout.gap)
        }
    }

    @Test fun squareCoverAlwaysAlignsWithRightColumn() {
        for (width in 260..600) {
            val layout = WidgetHeaderLayout.forWidth(width)
            assertTrue(layout.cover in 76..88)
            assertEquals(layout.cover, layout.height)
            assertTrue(layout.height - layout.buttonHeight >= 36)
        }
    }
}
