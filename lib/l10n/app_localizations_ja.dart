// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Japanese (`ja`).
class AppLocalizationsJa extends AppLocalizations {
  AppLocalizationsJa([String locale = 'ja']) : super(locale);

  @override
  String get untitled => 'タイトルなし';

  @override
  String get homeTitle => '音楽をホーム画面に';

  @override
  String get homeDescription => 'Samsung Music の再生キューを表示し、ホーム画面から選曲や再生操作ができます。';

  @override
  String get permissionError => '設定を開けませんでした。端末の設定から通知へのアクセスを許可してください。';

  @override
  String get permissionGranted => '通知へのアクセスが許可されています';

  @override
  String get firstPlayback => '初めに Samsung Music で曲を再生してください。';

  @override
  String get pinFallback => 'ホーム画面を長押しして、ウィジェット一覧から MusicBridge を追加してください。';

  @override
  String get addWidget => 'ホーム画面に追加';

  @override
  String get appearanceSettings => 'ウィジェットの外観設定';

  @override
  String get permissionTitle => '通知へのアクセスを許可';

  @override
  String get permissionDescription =>
      'Samsung Music の再生キューと再生状態を同期するには、通知へのアクセスが必要です。システム設定で MusicBridge を許可してください。';

  @override
  String get restrictedSettings =>
      '設定が制限されている場合は、「設定 → アプリ → MusicBridge」の右上のメニューに「制限付き設定を許可」があるか確認してください。インストール元を信頼できる場合にのみ許可し、その後、通知へのアクセスを有効にしてください。';

  @override
  String get openSettings => '設定を開く';

  @override
  String get appearanceTitle => 'ウィジェットの外観';

  @override
  String get appearanceSubtitle => 'ホーム画面に合わせて背景を調整できます。';

  @override
  String get wallpaperHint => '実際には端末の壁紙が透けて見えます。プレビューの背景はイメージです。';

  @override
  String get backgroundColor => '背景色';

  @override
  String get applyAll => '保存すると、すべての MusicBridge ウィジェットに適用されます。';

  @override
  String get retryAppearance => '外観設定を再読み込み';

  @override
  String get saved => '保存し、ウィジェットを更新しました';

  @override
  String get saveError => '設定を保存できませんでした';

  @override
  String get loadError => '設定を読み込めませんでした。もう一度お試しください。';

  @override
  String get followSystem => 'システム設定に合わせる';

  @override
  String get light => 'ライト';

  @override
  String get dark => 'ダーク';

  @override
  String get opacity => '不透明度';

  @override
  String get transparent => '透明';

  @override
  String get opaque => '不透明';

  @override
  String get cancel => 'キャンセル';

  @override
  String get saving => '保存中…';

  @override
  String get save => '保存';

  @override
  String get preview => '外観プレビュー・サンプル背景';

  @override
  String get demoQueue => 'サンプル曲・ホーム画面には実際の再生キューが表示されます';

  @override
  String get repeatOne => '1曲リピート';

  @override
  String get repeatAll => '全曲リピート';

  @override
  String get playOnce => '全曲を1回再生';

  @override
  String get repeatUnknown => '再生モードを取得できません';

  @override
  String get shuffleOff => 'シャッフル オフ';

  @override
  String get shuffleOn => 'シャッフル オン';

  @override
  String get shuffleUnknown => 'シャッフルの状態を取得できません';
}
