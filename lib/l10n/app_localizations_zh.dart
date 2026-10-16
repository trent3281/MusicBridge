// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Chinese (`zh`).
class AppLocalizationsZh extends AppLocalizations {
  AppLocalizationsZh([String locale = 'zh']) : super(locale);

  @override
  String get untitled => '未命名歌曲';

  @override
  String get homeTitle => '讓音樂回到桌面';

  @override
  String get homeDescription => '同步 Samsung Music 的播放佇列，直接在桌面選曲與控制播放。';

  @override
  String get permissionError => '無法開啟系統設定，請從手機設定開啟通知存取權。';

  @override
  String get permissionGranted => '通知存取權已開啟';

  @override
  String get firstPlayback => '首次使用請先在 Samsung Music 播放歌曲。';

  @override
  String get pinFallback => '請長按桌面，從小工具清單加入 MusicBridge。';

  @override
  String get addWidget => '新增至桌面';

  @override
  String get appearanceSettings => '小工具外觀設定';

  @override
  String get permissionTitle => '開啟通知存取權';

  @override
  String get permissionDescription =>
      'MusicBridge 需要通知存取權，才能同步 Samsung Music 的歌曲清單與播放狀態。請在系統設定中開啟 MusicBridge。';

  @override
  String get restrictedSettings =>
      '若系統禁止開啟，請先到「設定 → 應用程式 → MusicBridge」，查看右上角選單是否提供「允許受限制的設定」。僅在確認安裝來源可信時允許，再返回開啟通知存取權。';

  @override
  String get openSettings => '前往開啟';

  @override
  String get appearanceTitle => '小工具外觀';

  @override
  String get appearanceSubtitle => '調整背景，讓音樂融入你的桌面。';

  @override
  String get wallpaperHint => '背景會透出你自己的桌布；預覽圖片僅為示意。';

  @override
  String get backgroundColor => '背景色彩';

  @override
  String get applyAll => '儲存後套用至所有 MusicBridge 小工具。';

  @override
  String get retryAppearance => '重試載入外觀';

  @override
  String get saved => '已儲存，桌面小工具已更新';

  @override
  String get saveError => '儲存失敗';

  @override
  String get loadError => '無法載入設定，請稍後重試。';

  @override
  String get followSystem => '與手機設定同步';

  @override
  String get light => '淺色';

  @override
  String get dark => '深色';

  @override
  String get opacity => '不透明度';

  @override
  String get transparent => '完全透明';

  @override
  String get opaque => '完全不透明';

  @override
  String get cancel => '取消';

  @override
  String get saving => '儲存中…';

  @override
  String get save => '儲存';

  @override
  String get preview => '外觀預覽 · 示意背景';

  @override
  String get demoQueue => '示範歌曲 · 桌面只顯示真實播放佇列';

  @override
  String get repeatOne => '單曲循環';

  @override
  String get repeatAll => '全部循環';

  @override
  String get playOnce => '全部播放一次';

  @override
  String get repeatUnknown => '播放模式未公開';

  @override
  String get shuffleOff => '隨機播放關閉';

  @override
  String get shuffleOn => '隨機播放開啟';

  @override
  String get shuffleUnknown => '隨機播放狀態未公開';
}

/// The translations for Chinese, using the Han script (`zh_Hans`).
class AppLocalizationsZhHans extends AppLocalizationsZh {
  AppLocalizationsZhHans() : super('zh_Hans');

  @override
  String get untitled => '未命名歌曲';

  @override
  String get homeTitle => '让音乐回到桌面';

  @override
  String get homeDescription => '同步 Samsung Music 的播放队列，直接在桌面选曲与控制播放。';

  @override
  String get permissionError => '无法打开系统设置，请从手机设置开启通知使用权。';

  @override
  String get permissionGranted => '通知使用权已开启';

  @override
  String get firstPlayback => '首次使用请先在 Samsung Music 播放歌曲。';

  @override
  String get pinFallback => '请长按桌面，从小组件列表添加 MusicBridge。';

  @override
  String get addWidget => '添加到桌面';

  @override
  String get appearanceSettings => '小组件外观设置';

  @override
  String get permissionTitle => '开启通知使用权';

  @override
  String get permissionDescription =>
      'MusicBridge 需要通知使用权，才能同步 Samsung Music 的歌曲列表与播放状态。请在系统设置中开启 MusicBridge。';

  @override
  String get restrictedSettings =>
      '若系统禁止开启，请先到“设置 → 应用 → MusicBridge”，查看右上角菜单是否提供“允许受限设置”。仅在确认安装来源可信时允许，再返回开启通知使用权。';

  @override
  String get openSettings => '前往开启';

  @override
  String get appearanceTitle => '小组件外观';

  @override
  String get appearanceSubtitle => '调整背景，让音乐融入你的桌面。';

  @override
  String get wallpaperHint => '背景会透出你自己的壁纸；预览图片仅为示意。';

  @override
  String get backgroundColor => '背景颜色';

  @override
  String get applyAll => '保存后应用到所有 MusicBridge 小组件。';

  @override
  String get retryAppearance => '重试加载外观';

  @override
  String get saved => '已保存，桌面小组件已更新';

  @override
  String get saveError => '保存失败';

  @override
  String get loadError => '无法加载设置，请稍后重试。';

  @override
  String get followSystem => '跟随系统';

  @override
  String get light => '浅色';

  @override
  String get dark => '深色';

  @override
  String get opacity => '不透明度';

  @override
  String get transparent => '完全透明';

  @override
  String get opaque => '完全不透明';

  @override
  String get cancel => '取消';

  @override
  String get saving => '保存中…';

  @override
  String get save => '保存';

  @override
  String get preview => '外观预览 · 示意背景';

  @override
  String get demoQueue => '示范歌曲 · 桌面仅显示实际播放队列';

  @override
  String get repeatOne => '单曲循环';

  @override
  String get repeatAll => '全部循环';

  @override
  String get playOnce => '全部播放一次';

  @override
  String get repeatUnknown => '播放模式未公开';

  @override
  String get shuffleOff => '随机播放关闭';

  @override
  String get shuffleOn => '随机播放开启';

  @override
  String get shuffleUnknown => '随机播放状态未公开';
}

/// The translations for Chinese, using the Han script (`zh_Hant`).
class AppLocalizationsZhHant extends AppLocalizationsZh {
  AppLocalizationsZhHant() : super('zh_Hant');

  @override
  String get untitled => '未命名歌曲';

  @override
  String get homeTitle => '讓音樂回到桌面';

  @override
  String get homeDescription => '同步 Samsung Music 的播放佇列，直接在桌面選曲與控制播放。';

  @override
  String get permissionError => '無法開啟系統設定，請從手機設定開啟通知存取權。';

  @override
  String get permissionGranted => '通知存取權已開啟';

  @override
  String get firstPlayback => '首次使用請先在 Samsung Music 播放歌曲。';

  @override
  String get pinFallback => '請長按桌面，從小工具清單加入 MusicBridge。';

  @override
  String get addWidget => '新增至桌面';

  @override
  String get appearanceSettings => '小工具外觀設定';

  @override
  String get permissionTitle => '開啟通知存取權';

  @override
  String get permissionDescription =>
      'MusicBridge 需要通知存取權，才能同步 Samsung Music 的歌曲清單與播放狀態。請在系統設定中開啟 MusicBridge。';

  @override
  String get restrictedSettings =>
      '若系統禁止開啟，請先到「設定 → 應用程式 → MusicBridge」，查看右上角選單是否提供「允許受限制的設定」。僅在確認安裝來源可信時允許，再返回開啟通知存取權。';

  @override
  String get openSettings => '前往開啟';

  @override
  String get appearanceTitle => '小工具外觀';

  @override
  String get appearanceSubtitle => '調整背景，讓音樂融入你的桌面。';

  @override
  String get wallpaperHint => '背景會透出你自己的桌布；預覽圖片僅為示意。';

  @override
  String get backgroundColor => '背景色彩';

  @override
  String get applyAll => '儲存後套用至所有 MusicBridge 小工具。';

  @override
  String get retryAppearance => '重試載入外觀';

  @override
  String get saved => '已儲存，桌面小工具已更新';

  @override
  String get saveError => '儲存失敗';

  @override
  String get loadError => '無法載入設定，請稍後重試。';

  @override
  String get followSystem => '與手機設定同步';

  @override
  String get light => '淺色';

  @override
  String get dark => '深色';

  @override
  String get opacity => '不透明度';

  @override
  String get transparent => '完全透明';

  @override
  String get opaque => '完全不透明';

  @override
  String get cancel => '取消';

  @override
  String get saving => '儲存中…';

  @override
  String get save => '儲存';

  @override
  String get preview => '外觀預覽 · 示意背景';

  @override
  String get demoQueue => '示範歌曲 · 桌面只顯示真實播放佇列';

  @override
  String get repeatOne => '單曲循環';

  @override
  String get repeatAll => '全部循環';

  @override
  String get playOnce => '全部播放一次';

  @override
  String get repeatUnknown => '播放模式未公開';

  @override
  String get shuffleOff => '隨機播放關閉';

  @override
  String get shuffleOn => '隨機播放開啟';

  @override
  String get shuffleUnknown => '隨機播放狀態未公開';
}
