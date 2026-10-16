package com.example.music_bridge

/** Launcher 的設定入口獨立於 App 首頁，重設外觀時直接進入設定路由。 */
class WidgetSettingsActivity : MainActivity() {
    override fun getInitialRoute(): String = "/settings"
}
