# MusicBridge

MusicBridge 是專為 Samsung Music 打造的 Android 桌面小工具，補回 Android 16／One UI 8 起移除的歌曲清單式 Widget，讓使用者直接在桌面瀏覽播放佇列、選擇歌曲與控制播放。

外觀設定頁以 Flutter 開發，桌面小工具與媒體整合由 Kotlin 實作，音樂播放交由 Samsung Music 處理。支援 Android 12（API 31）以上，無須 Root。

## 版本與相容性

| 項目 | 版本／條件 |
| --- | --- |
| MusicBridge | 0.1.0（build 1，對應 `pubspec.yaml` 的 `0.1.0+1`） |
| 最低 Android 版本 | Android 12（API 31） |
| 實際播放器 | Samsung Music（`com.sec.android.app.music`） |
| 清單式 Widget 停止提供的系統版本 | Android 16／One UI 8 |
| 列出該限制的 Samsung Music 版本 | 16.2.41 系列 |

## 專案動機

Samsung Music 在 Android 15／One UI 7 環境下提供可顯示目前播放清單的 4×2 Widget，能在桌面捲動歌曲列表並直接選曲。從 Android 16／One UI 8 起，Samsung Music 停止提供這個清單式 Widget；16.2.41 系列的更新說明也列出自 Android B OS（Android 16）起不再支援 4×2 Widget。

MusicBridge 延續這項桌面操作體驗，透過 Samsung Music 公開的媒體介面顯示播放佇列、標示目前歌曲，並提供直接選曲、播放控制及背景外觀調整。

本專案是獨立開發的第三方工具，與 Samsung 無隸屬關係。可取得的資料與控制能力仍受 Samsung Music 公開介面限制，詳見下方「限制」。

參考資料：[三星官方社群的移除確認](https://eu.community.samsung.com/t5/galaxy-s24-series/samsung-music-widget-scomparso/td-p/13317912)、[Samsung Music 16.2.41 更新紀錄存檔](https://www.apkmirror.com/apk/samsung-electronics-co-ltd/music-samsung-electronics-co-ltd/samsung-music-16-2-41-9-release/)。

## 專案結構

模組職責、資料流與實作細節見 [架構與實作說明](docs/FLUTTER_GUIDE.md)。

```text
lib/
  main.dart                       啟動入口
  app.dart                        全域主題與首頁
  controllers/bridge_controller.dart  頁面狀態、草稿、儲存與輪詢
  models/                         外觀設定、歌曲與播放快照
  services/music_bridge_api.dart  Flutter ↔ Kotlin 平台通訊
  pages/bridge_page.dart          設定頁組合與生命週期
  widgets/                        設定卡片、預覽與播放模式圖示
android/app/src/main/
  kotlin/com/example/music_bridge/  原生媒體控制、背景服務與桌面繪製
  res/layout/                      真正桌面 Widget 的 XML 版面
```

Flutter 畫面與桌面小工具是兩套 UI。設定頁預覽是 Flutter；桌面使用 Android RemoteViews。修改外觀時請同步檢查兩邊。

## 0.1.0 功能

- 專輯圖、目前歌名／演出者、可捲動的公開播放佇列與目前歌曲高亮。
- 頂部長歌名使用原生跑馬燈；短歌名維持靜止。
- 播放／暫停、上／下一首、隨機、單曲循環／全部循環／全部播放一次，依 Samsung Music 公開能力啟用。
- 背景色跟隨系統／淺色／深色，不透明度 0～100%；字體固定為原大小的 90%。
- 儲存套用至所有 MusicBridge 小工具，取消只還原設定草稿。
- Samsung Music 關閉後保留最後清單；點歌曲可背景恢復連線後播放，不建立 Activity 視窗。
- 外部播放器狀態變動透過媒體回呼、通知訊號與短週期補查更新桌面。
- 桌面最多顯示前 150 首以控制 Binder 傳輸大小；設定頁只保留外觀預覽與調整。

## 手機使用

1. 在手機系統設定中開啟 MusicBridge 的通知存取權。
2. 第一次先在 Samsung Music 播放歌曲，讓 MusicBridge 取得有效清單。
3. 長按桌面 → 小工具 → MusicBridge，加入桌面小工具。
4. 在設定頁調整外觀並儲存；長按桌面小工具也可進入設定。
5. 點頂部資訊區開啟 Samsung Music；點歌曲列則直接選曲播放。
6. 如果背景更新延遲，從手機系統設定確認 MusicBridge 可在背景執行。

## 限制

Samsung Music 在隨機模式下的內部播放順序，可能不同於對外 MediaSession 公開清單。本專案不自行洗牌冒充同步。
離線資料只是快取；重連後會比對最新佇列，以穩定歌曲身分選曲，不使用過期 queue ID。歌曲已移除或比對有歧義時不猜測播放。
實際通知存取權被撤銷時，仍需使用者重新開啟；離線不等於未授權。

## 建置與驗證

App 圖示原圖放在 `assets/icons/app_icon.png`。替換原圖後執行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\generate_app_icons.ps1
```

腳本會產生 Android 各密度的 `mipmap-*/ic_launcher.png`，由 Manifest 的 `@mipmap/ic_launcher` 引用。圖示變更後需重新建置安裝。

在專案根目錄執行：

```powershell
flutter doctor
flutter analyze
flutter test
flutter build apk --debug
```

Android 純邏輯測試：

```powershell
.\android\gradlew.bat -p android :app:testDebugUnitTest --console=plain
```

APK：`build/app/outputs/flutter-apk/app-debug.apk`。
修改 Kotlin、Android XML 或 Manifest 後需要重新建置安裝，不能只用 Flutter hot reload。

本機已配置 Flutter、Android Studio、JDK 17 與 Android SDK。若在新環境缺少 scaffold，可使用 `setup.ps1` 補缺少檔案；不要直接執行 `flutter create .` 覆蓋自訂整合。

環境設定與歷次真機測試見 [開發歷史](docs/development_history.md)。該文件保留各開發階段的記錄；0.1.0 的功能與限制以本 README 為準。

## 商標與第三方內容

MusicBridge 為獨立開發的第三方專案，並非 Samsung 官方產品，未獲 Samsung 贊助、認可或背書。文中的 Samsung、Samsung Music 及相關商標屬於各自權利人；產品名稱用於說明本專案的相容對象與整合用途。

第三方程式庫、工具及素材依各自授權條款使用。本專案不授予 Samsung 商標、官方圖示、音樂、專輯封面或其他第三方內容的使用權；重製與散布這些內容，仍須確認相應授權。

## 程式碼授權

本專案目前尚未指定開源授權，也未提供 `LICENSE` 檔案。公開原始碼不代表授予任意使用、修改或再散布的權利；GitHub 平台條款所允許的瀏覽與 fork 不受此說明影響。

如採用開源授權，將在根目錄提供完整 `LICENSE`，並在本節註明其適用範圍。第三方內容仍依各自授權處理。
