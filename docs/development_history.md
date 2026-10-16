# 開發歷史紀錄

以下保留先前各階段記錄，可能包含已被取代的設計；目前規格以 README.md 和實際程式為準。

# MusicBridge

Flutter 設定／診斷頁 + Kotlin Android 桌面播放清單 Widget 原型。Android 12（API 31）以上；不需要 Root。實際播放器固定為 Samsung Music（com.sec.android.app.music）。

## 建置

此電腦已安裝 Flutter 3.47.7 stable（Dart 3.13.5）、Android Studio 2026.2.1、Temurin JDK 17、Android SDK 36、Build Tools 36.0.0、NDK 28.2.13676358 與 CMake 3.22.1。Android 工具鏈及授權檢查已通過，flutter analyze 無問題。flutter build apk --debug 已成功，APK 位於 build/app/outputs/flutter-apk/app-debug.apk。尚未進行 Samsung 手機實測。

1. 安裝 Flutter stable 與 Android Studio，設定 Android SDK / JDK 17 或 Flutter 指定的相容 JDK。
2. 執行 flutter doctor，完成 Android 授權及環境檢查。
3. 在此資料夾執行 powershell -ExecutionPolicy Bypass -File .\setup.ps1。
4. 執行 flutter analyze。
5. 連接手機並啟用 USB 偵錯，執行 flutter run；或 flutter build apk --debug。
6. APK 路徑：build/app/outputs/flutter-apk/app-debug.apk。

setup.ps1 使用目前安裝的 Flutter 產生 Android 工具鏈檔案（含 Gradle wrapper、圖示與主題），只補上缺少的檔案。自訂的 manifest、Kotlin、app Gradle 與 lib 不會被覆寫。請勿用 flutter create . 取代此流程。再次執行不會更新既有 scaffold。

## 手機上操作

1. 開啟 MusicBridge → 通知存取權 → 允許 MusicBridge。
2. 開啟 Samsung Music，播放任一歌曲，再返回 MusicBridge。
3. 檢查連線狀態、歌曲、動作旗標、佇列數量與循環模式。
4. 點加入桌面，或長按桌面 → 小工具 → MusicBridge。
5. 點 Widget 歌名區域開啟 Samsung Music；底部設定開啟 MusicBridge。
6. 預設建議 4×5，可自由縮放；清單可上下捲動。

## 已實作

- Samsung Music session 探索與變更監聽、播放資訊與佇列 callback。
- 上一首、下一首、播放／暫停；依公布的 action flags 啟用。
- 公開佇列、目前歌曲標記、skipToQueueItem 選曲。
- 循環切換：關閉 → 全部 → 單曲 → 關閉。使用 MediaControllerCompat 的標準 repeat API，必須由播放器公開 ACTION_SET_REPEAT_MODE。
- Android 12+ RemoteCollectionItems 可捲動桌面清單；最多顯示前 150 筆以限制 Binder 負載，完整診斷清單在 App。
- Widget 背景透明度（所有實例共用）、系統固定 Widget 請求。

## 必須實測的限制

Samsung Music 版本可能不公開 queue、queue IDs 或 repeat mode，也可能忽略指令。空佇列不代表手機沒有音樂。本原型不讀取 MediaStore、不猜測私有廣播，不用自建清單假裝同步。若未公布能力，按鈕停用；循環顯示「?」代表未知。狀態以播放器回傳為準，按下指令不會自行假造成功狀態。

androidx.media 是舊版相容 API，此處用於連接其他 App 的既有 MediaSession 並取得 repeat mode。相容層預設的 repeat=0 也不能證明播放器確實有提供循環狀態，需配合 action flags 與實際觀察。

僅以通知存取權取得 Samsung Music 的 MediaSession，不讀取通知內容、不連線上傳。通知存取權本身是系統廣泛權限，請於系統頁面確認後授予。強制停止 MusicBridge、撤銷權限或 Samsung Music 關閉 session 後，不保證桌面能持續更新；重新開啟 App / 播放音樂。

## 驗收

- 未授權、Samsung Music 未安裝、已安裝但未播放：應顯示明確狀態、不當機。
- Samsung Music 播放／暫停／換歌：App 與桌面應更新。
- 佇列為空：顯示未公開；有佇列：順序一致，點選可切換（若支援）。
- 在 Samsung Music 改循環，再在 Widget 改循環，觀察兩邊實際狀態。
- 調整 Widget 尺寸、透明度，重啟手機及撤銷／重新授權後再測。
- 如果無法同步，記下 Samsung Music 版本、Android / One UI 版本及診斷旗標。

## 參考

- https://developer.android.com/develop/ui/views/appwidgets/collections
- https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat

## 參考圖外觀更新

- 圖一風格：圓角半透明底板、音符、五個圖示控制、無分隔線歌曲清單。桌布由桌面自然透出，不內嵌參考圖。
- 背景色彩：跟隨系統／淺色／深色。不透明度 0–100%，只調整背景。
- 外觀頁提供即時預覽、取消與儲存；所有 Widget 共用設定並在重開後還原。預覽未取得佇列時明確標示示範歌曲。
- 可從 Widget 右下角或支援的桌面長按設定進入。隨機播放依 Samsung Music 公開能力啟用。
- 驗證：2 個設定互動測試通過、flutter analyze 通過、debug APK 建置成功。桌面實際顯示與 Samsung Music 控制仍需手機驗證。

## 專輯圖與字體更新

- 頂部改為左側方形專輯圖，右側上方歌名／歌手、下方五個控制鍵。
- 讀取 Samsung Music 公開的內嵌封面，或具有讀取權限的 content/file/resource 圖片 URI；不下載網路圖片。未公開或無權讀取時使用音符。圖片在背景執行緒載入、縮放至 256px，切歌時會清除上一首封面。
- 字體大小 80–140%，作用於頂部與歌曲清單中的歌名、歌手；預覽、取消與持久儲存皆已接入。
- 驗證：3 個互動測試（含 360px 窄螢幕最大字體）通過，flutter analyze 與 APK 建置通過。手機封面來源與桌面顯示仍需實測。

## 固定字體與播放模式更新

- 字體固定 90%，移除字體滑桿；舊字體設定不再影響桌面。淺色歌手文字加深至 #242428。
- 移除右下角漢堡與其佔用空間，保留桌面長按的重新設定入口。
- 單曲循環 1 → 全部循環 A → 全部播放一次 A→；分別對應 MediaSession REPEAT_MODE_ONE、ALL、NONE，狀態仍由 Samsung Music 回報。
- 四項互動測試與分析通過。

## 隨機播放與控制圖示調整

- 隨機播放開啟使用交叉箭頭，關閉使用交叉箭頭加斜線，依 Samsung Music 回傳模式同步；未知狀態不視為關閉。
- 五個控制圖示縮小為前一版的 95%，點擊範圍與排版保留。預覽同步調整。

## 控制圖示再縮小

五個播放控制圖示再比上一版縮小 20%，目前為最初大小的 76%；點擊範圍、位置與間距保留。

## 清單排列更新

清單恢復緊湊雙行排列：固定左側播放箭頭欄位，右側歌名與演出者對齊；目前播放箭頭使用獨立大三角圖示。文字大小及深淺色維持前版，頂部專輯圖與五個控制鍵保留。

## 2026-10-16 Samsung 真機隨機播放診斷

- 已在連接的 SM-S9480 實測：標準 setShuffleMode 可讓 Samsung Music 回傳模式 0 → 1；從 Samsung Music 畫面切換也能收到 1 → 0 回報。
- Samsung Music 畫面實際隨機清單與 MediaSession 公開清單不同。隨機切換前後公開佇列均為 276 首，queue ID 順序完全不變。MediaBrowser 服務拒絕第三方連線。
- 因此無法只靠目前可取得的公開介面同步內部隨機佇列。本版不自行洗牌，也不把隨機模式成功誤稱為清單同步成功。
- 新增指令後 0.2/0.7/1.5/3.2/5.5 秒讀取、通知監聽服務連線期間每 2 秒觀察、queue callback 直接採用回報清單、群組隨機切換修正。
- 診斷頁可查看模式確認結果、清單版本、前 8 個 queue ID 與 16 筆事件，以及公開自訂操作。記錄僅保存在 App 內部 files/media-diagnostics.json，不上傳。
- 目前播放三角形從 20dp 縮至 16dp，歌名左側對齊不變。5 項 Android 比對單元測試通過。

## 離線清單保留與連線恢復

- 最後一次有效佇列、歌名與歌手儲存到 App 私有 SharedPreferences，播放器結束或 MusicBridge 程序重啟後仍可顯示。關閉時的空清單／空歌名不會覆蓋已儲存資料。
- 離線快取不恢復播放中狀態或操作權限；點歌先重連，再以歌曲身分比對最新佇列播放，不使用過期 queue ID。播放鍵直接開啟 Samsung Music。重新連線後以即時資料取代。
- 分開呈現真正未授權與播放器離線，新增通知監聽斷線後重綁、開機／更新還原及背景連線重查。使用者真正撤銷的通知存取權仍需自行重新授予，App 不會自行變更權限。
- 真機驗證：Samsung Music 關閉後 connected=false、permission=true、cached=true，保留 276 首；MusicBridge 程序重新啟動且未開啟 Activity 後仍保留同樣清單與歌名。
- 7 項清單快取單元測試、5 項隨機比對單元測試、4 項 Flutter 測試通過；分析與 APK 建置通過。未實際重開整支手機。

## 目前歌曲高亮與外部模式即時同步

- 循環控制鍵不再依模式變色；僅目前清單列的歌名、演出者與三角形使用強調色。其他大小與排列不變，預覽一致。
- 修正只比對隨機／清單而漏掉循環的重新繪製條件；播放模式、目前項目、播放狀態與動作能力都納入比對。回呼直接採用回報值，並讀取已在真機確認的 Samsung session extras。
- 監聽 Samsung Music 通知的新增／移除作為額外同步訊號，只判斷 package，不讀通知內容；保留 2 秒背景補查。
- 真機另外確認主執行緒曾停於 do_freezer_trap；僅對 MusicBridge 加入 deviceidle 省電豁免並喚醒既有程序後，再測外部循環 370ms、隨機 344ms 即完成更新，activeId 皆不變。未關閉全機省電功能。
- 設定頁新增背景執行設定入口；其他手機若有背景凍結仍可能需要不受限制／永不休眠設定。
- 真機測試模式還原為 repeat=2、shuffle=1。16 項 Android 單元測試與 5 項 Flutter 測試通過。

## 關閉 Samsung Music 後點歌

- Widget 及 App 診斷清單的歌曲點擊統一走原生選曲流程；沒有連線時只向 Samsung Music 的媒體按鍵接收器發送 PLAY，在背景恢復連線，最多等候 8 秒重新取得目前佇列，再 skipToQueueItem + play。不啟動 Samsung Music 的 Activity。
- 快取保存 mediaId／mediaUri，優先用穩定歌曲身分匹配新佇列；舊快取只在歌名＋演出者唯一相符時使用。找不到／有歧義不猜測，逾時顯示提示。
- Widget 使用 Broadcast PendingIntent，goAsync 在短暫連線期間保留廣播工作；連續點歌以最後一次為準，取消舊等待並結束舊廣播。只向 Samsung Music 發送喚醒媒體鍵，不控制其他播放器。
- 真機驗證：原先離線快取點「わたし」可開啟並播放；強制停止 Samsung Music 後，點「リナリア」亦自動播放，connected=true、cached=false、playing=true。冷啟動前後 queue ID 不同，仍選到正確歌曲。
- 保留既有限制：只能比對 Samsung Music 對外提供的目前佇列；已移除的歌曲或未公開的內部隨機順序無法靠快取補成即時資料。

## 未開啟播放器時直接背景點歌

- 更正先前會跳轉 Samsung Music 的流程：移除選曲流程中的播放器 launch Intent，只有頂部標題等既有開啟 App 操作仍會進入播放器。
- 選曲不建立任何 Activity 或視窗；已刪除會造成閃動的透明 Activity、Manifest 宣告及對應主題，改用背景接收器與短時間重試。
- 真機實測：force-stop Samsung Music 後從桌面點「リナリア」，Samsung Music 背景程序重新建立，connected=true、cached=false、playing=true；topResumedActivity 為 Samsung Launcher，沒有開啟 Samsung Music 畫面。
- 接收器版本已再次通過同樣的真機冷啟動點歌測試及 21 項 Android 單元測試。

## 頂部區塊對齊舊版配置

- 左側專輯圖與右側整組同為 88dp 高，取消原本以 64dp 專輯圖置中對齊較高內容的做法。
- 右側上區歌名／演出者靠上對齊專輯圖，移除原生文字額外字型邊距；下區保留 48dp 控制列，五鍵等分寬度並對齊整組底部。
- 保留現有文字大小、圖示縮放、顏色與清單排版；Flutter 設定預覽同步區塊配置，明確行高避免預覽溢出。
- 5 項 Flutter 介面測試與靜態分析通過，APK 建置及手機更新完成。

## 頂部歌名跑馬燈

- 頂部目前歌曲名稱改用單行原生 marquee、無限循環；文字超出可用寬度才捲動，短歌名維持靜止。
- 以原生文字焦點啟動跑馬燈，沒有加入定時重繪 Widget 或新的 Activity；演出者、清單列及原有字體大小／顏色維持原設定。
- 真機以 Every Second (Japanese Version) 比較相隔兩秒的桌面截圖，已確認歌名向左移動並循環帶回開頭；驗證完成後恢復測試前歌曲及播放狀態。APK 建置及更新完成。
