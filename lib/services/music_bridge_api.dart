import 'package:flutter/services.dart';
import '../models/appearance_settings.dart';
import '../models/playback_snapshot.dart';

/// Flutter 與 Kotlin 之間唯一的通訊入口。
/// MethodChannel 的名稱與方法名稱必須對應 Android MainActivity.kt。
/// 畫面元件不直接呼叫平台 API，換通訊方式時只需要修改這一層。
class MusicBridgeApi {
  const MusicBridgeApi();
  static const _channel = MethodChannel('music_bridge/media');

  /// 通知存取權是系統特殊存取權，需由使用者在系統頁面開啟。
  Future<void> openNotificationSettings() =>
      _channel.invokeMethod<void>('permission');

  /// 交由 Launcher 顯示新增確認，不會自行重複建立桌面小工具。
  Future<void> requestWidgetPin() => _channel.invokeMethod<void>('pin');

  Future<AppearanceSettings> loadAppearance() async {
    final map = await _channel.invokeMapMethod<String, dynamic>('appearance');
    return AppearanceSettings.fromMap(map ?? {});
  }

  Future<void> saveAppearance(AppearanceSettings settings) =>
      _channel.invokeMethod<void>('saveAppearance', settings.toMap());

  Future<PlaybackSnapshot> loadSnapshot() async {
    final map = await _channel.invokeMapMethod<String, dynamic>('snapshot');
    return PlaybackSnapshot(map ?? {});
  }

  /// Uint8List 是位元組陣列，可交給 Image.memory 顯示專輯圖。
  Future<Uint8List?> loadArtwork() =>
      _channel.invokeMethod<Uint8List>('artwork');
}
