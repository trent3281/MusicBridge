import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/appearance_settings.dart';
import '../models/playback_snapshot.dart';
import '../services/music_bridge_api.dart';

/// 管理頁面資料，沒有 BuildContext，也不建立任何畫面。
/// ChangeNotifier 的 notifyListeners() 會通知頁面重新 build；
/// 採用 Flutter 內建狀態通知機制，將資料操作與畫面元件分離。
class BridgeController extends ChangeNotifier {
  BridgeController({MusicBridgeApi api = const MusicBridgeApi()}) : _api = api;
  final MusicBridgeApi _api;
  Timer? _timer;
  bool _disposed = false;
  bool _refreshing = false;
  int _artworkRevision = -1;

  AppearanceSettings draft = const AppearanceSettings();
  AppearanceSettings _saved = const AppearanceSettings();
  PlaybackSnapshot snapshot = PlaybackSnapshot({});
  Uint8List? artwork;
  bool loaded = false;
  bool saving = false;
  String? error;

  bool get canEdit => loaded && !saving;

  /// 頁面打開才開始輪詢；這只更新 Flutter 預覽。
  /// 桌面 Widget 的背景同步由 Android MusicListener 負責，與此 Timer 無關。
  void start() {
    unawaited(loadAppearance());
    unawaited(refresh());
    _timer ??= Timer.periodic(const Duration(seconds: 2), (_) => refresh());
  }

  void _notify() {
    if (!_disposed) notifyListeners();
  }

  Future<void> loadAppearance() async {
    try {
      final settings = await _api.loadAppearance();
      if (_disposed) return;
      draft = _saved = settings;
      loaded = true;
      error = null;
    } catch (e) {
      if (!_disposed) error = '無法載入外觀設定：$e';
    }
    _notify();
  }

  Future<void> refresh() async {
    // 非同步呼叫可能比兩秒更久；上一輪未完成就不再送第二次。
    if (_disposed || _refreshing) return;
    _refreshing = true;
    try {
      final next = await _api.loadSnapshot();
      if (_disposed) return;
      // 專輯圖通常比文字大，只有版本不同時才重新跨平台取得圖片。
      final image = next.artworkRevision != _artworkRevision
          ? await _api.loadArtwork()
          : artwork;
      if (_disposed) return;
      snapshot = next;
      artwork = image;
      _artworkRevision = next.artworkRevision;
    } catch (e) {
      if (!_disposed) error = e.toString();
    } finally {
      _refreshing = false;
    }
    _notify();
  }

  void changeMode(String mode) {
    draft = draft.copyWith(mode: mode);
    _notify();
  }

  void changeOpacity(double opacity) {
    draft = draft.copyWith(opacity: opacity);
    _notify();
  }

  /// 取消只還原草稿，完全不呼叫 Kotlin 儲存。
  void cancel() {
    draft = _saved;
    _notify();
  }

  Future<void> save() async {
    saving = true;
    _notify();
    final submitted = draft;
    try {
      await _api.saveAppearance(submitted);
      if (!_disposed) _saved = submitted;
    } finally {
      // finally 不論成功或失敗都會執行，避免按鈕永遠停在「儲存中」。
      saving = false;
      _notify();
    }
  }

  @override
  void dispose() {
    // await 途中頁面可能已關閉；旗標阻止完成後再通知已銷毀的畫面。
    _disposed = true;
    _timer?.cancel();
    super.dispose();
  }
}
