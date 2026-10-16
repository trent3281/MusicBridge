/// 一份外觀設定。不可變（immutable）表示建立後不直接修改欄位，
/// 修改時用 copyWith 建立新物件，方便分清「編輯草稿」和「已儲存設定」。
class AppearanceSettings {
  const AppearanceSettings({this.mode = 'dark', this.opacity = .35});

  /// 名稱必須和 Kotlin SharedPreferences 使用的字串相同。
  final String mode;

  /// 0 是完全透明，1 是完全不透明；不是 0～100。
  final double opacity;

  /// 使用者已決定固定為原始字體大小的 90%，不是設定頁的可調選項。
  static const fontScale = .9;

  factory AppearanceSettings.fromMap(Map<String, dynamic> map) =>
      AppearanceSettings(
        mode: map['mode'] as String? ?? 'dark',
        opacity: (map['opacity'] as num?)?.toDouble() ?? .35,
      );

  AppearanceSettings copyWith({String? mode, double? opacity}) =>
      AppearanceSettings(
          mode: mode ?? this.mode, opacity: opacity ?? this.opacity);

  /// 平台通訊只能傳遞簡單型別，所以送出前轉成 Map。
  Map<String, Object> toMap() => {'mode': mode, 'opacity': opacity};

  bool isDark({required bool systemIsDark}) =>
      mode == 'dark' || (mode == 'system' && systemIsDark);
}
