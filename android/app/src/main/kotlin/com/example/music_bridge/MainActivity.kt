package com.example.music_bridge

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Flutter 的 Android 宿主，也是 MethodChannel 的接線處。
 * Dart 端 MusicBridgeApi 呼叫的方法名稱，必須與下方 when 分支完全一致。
 * 媒體監聽留在 MusicListener，不依賴這個設定頁保持開啟。
 */
open class MainActivity : FlutterActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        MusicWidget.updateAll(this)
    }
    override fun configureFlutterEngine(engine: FlutterEngine) {
        super.configureFlutterEngine(engine)
        Bridge.init(applicationContext)
        // binaryMessenger 負責在 Dart 與 Kotlin 之間傳遞訊息。
        // 每次呼叫都必須回覆 success 或 error，否則 Dart 的 Future 會一直等待。
        MethodChannel(engine.dartExecutor.binaryMessenger, "music_bridge/media").setMethodCallHandler { call, result ->
            try {
                when (call.method) {
                    "backgroundSettings" -> {
                        val battery = Intent("android.settings.APP_BATTERY_SETTINGS",
                            android.net.Uri.parse("package:$packageName"))
                        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:$packageName"))
                        startActivity(if (battery.resolveActivity(packageManager) != null) battery else fallback)
                        result.success(null)
                    }
                    "inspect" -> { Bridge.inspect(); result.success(null) }
                    "artwork" -> result.success(Artwork.bytes)
                    "appearance" -> {
                        val prefs = getSharedPreferences("widget", MODE_PRIVATE)
                        result.success(mapOf("mode" to prefs.getString("mode", "dark"),
                            "opacity" to prefs.getFloat("opacity", .35f).toDouble()))
                    }
                    "saveAppearance" -> {
                        // SharedPreferences 是 Android 的少量設定儲存區。
                        // 先寫入，再要求所有桌面 Widget 依新設定重畫。
                        val mode = call.argument<String>("mode") ?: "dark"
                        require(mode in listOf("system", "light", "dark"))
                        val value = call.argument<Number>("opacity")?.toFloat() ?: .35f
                        getSharedPreferences("widget", MODE_PRIVATE).edit()
                            .putString("mode", mode).putFloat("opacity", value.coerceIn(0f, 1f))
                            .remove("fontScale").apply()
                        MusicWidget.updateAll(this)
                        result.success(null)
                        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            // 從 Launcher「小工具設定」開啟時，儲存後要回傳 widgetId。
                            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                            finish()
                        }
                    }
                    "snapshot" -> { Bridge.connect(); result.success(Bridge.snapshot()) }
                    "permission" -> {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        result.success(null)
                    }
                    "select" -> {
                        // 不開 Samsung Music 畫面；由背景流程恢復連線並比對歌曲。
                        BackgroundPlayback.select(this, SongSelection(call.argument<String>("mediaId"),
                            call.argument<String>("uri"), call.argument<String>("title").orEmpty(),
                            call.argument<String>("artist").orEmpty()))
                        result.success(null)
                    }
                    "launch" -> {
                        val launch = packageManager.getLaunchIntentForPackage(Bridge.PACKAGE)
                        if (launch == null) result.error("NOT_INSTALLED", getString(R.string.music_missing), null)
                        else { startActivity(launch); result.success(null) }
                    }
                    "pin" -> {
                        val manager = getSystemService(AppWidgetManager::class.java)
                        if (manager.isRequestPinAppWidgetSupported) {
                            manager.requestPinAppWidget(ComponentName(this, MusicWidget::class.java), null, null)
                            result.success(null)
                        } else result.error("PIN_UNSUPPORTED", getString(R.string.pin_manual), null)
                    }
                    "opacity" -> {
                        val value = call.argument<Number>("value")?.toFloat() ?: 0.88f
                        getSharedPreferences("widget", MODE_PRIVATE).edit().putFloat("opacity", value.coerceIn(.15f, 1f)).apply()
                        MusicWidget.updateAll(this)
                        result.success(null)
                    }
                    else -> { Bridge.command(call.method, call.argument<Number>("id")?.toLong()); result.success(null) }
                }
            } catch (e: Exception) {
                result.error("MEDIA_ERROR", e.message, null)
            }
        }
    }
}
