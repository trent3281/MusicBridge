// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get untitled => 'Untitled track';

  @override
  String get homeTitle => 'Bring music to your home screen';

  @override
  String get homeDescription =>
      'Browse Samsung Music’s playback queue and control playback from your home screen.';

  @override
  String get permissionError =>
      'Could not open settings. Enable notification access in your device settings.';

  @override
  String get permissionGranted => 'Notification access enabled';

  @override
  String get firstPlayback => 'To get started, play a song in Samsung Music.';

  @override
  String get pinFallback =>
      'Touch and hold your home screen, then add MusicBridge from Widgets.';

  @override
  String get addWidget => 'Add to home screen';

  @override
  String get appearanceSettings => 'Widget appearance settings';

  @override
  String get permissionTitle => 'Enable notification access';

  @override
  String get permissionDescription =>
      'MusicBridge needs notification access to sync Samsung Music’s queue and playback status. Enable MusicBridge in system settings.';

  @override
  String get restrictedSettings =>
      'If access is restricted, open Settings → Apps → MusicBridge and check the top-right menu for “Allow restricted settings”. Only allow this if you trust the installation source, then enable notification access.';

  @override
  String get openSettings => 'Open settings';

  @override
  String get appearanceTitle => 'Widget appearance';

  @override
  String get appearanceSubtitle =>
      'Customize the background to match your home screen.';

  @override
  String get wallpaperHint =>
      'Your wallpaper shows through the background. This preview is illustrative.';

  @override
  String get backgroundColor => 'Background color';

  @override
  String get applyAll => 'Saving applies to all MusicBridge widgets.';

  @override
  String get retryAppearance => 'Retry loading appearance';

  @override
  String get saved => 'Saved. Home screen widgets updated.';

  @override
  String get saveError => 'Could not save settings';

  @override
  String get loadError => 'Could not load settings. Please try again.';

  @override
  String get followSystem => 'Follow system';

  @override
  String get light => 'Light';

  @override
  String get dark => 'Dark';

  @override
  String get opacity => 'Opacity';

  @override
  String get transparent => 'Transparent';

  @override
  String get opaque => 'Opaque';

  @override
  String get cancel => 'Cancel';

  @override
  String get saving => 'Saving…';

  @override
  String get save => 'Save';

  @override
  String get preview => 'Appearance preview · sample background';

  @override
  String get demoQueue => 'Sample songs · the widget shows your actual queue';

  @override
  String get repeatOne => 'Repeat one';

  @override
  String get repeatAll => 'Repeat all';

  @override
  String get playOnce => 'Play all once';

  @override
  String get repeatUnknown => 'Playback mode unavailable';

  @override
  String get shuffleOff => 'Shuffle off';

  @override
  String get shuffleOn => 'Shuffle on';

  @override
  String get shuffleUnknown => 'Shuffle status unavailable';
}
