import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_ja.dart';
import 'app_localizations_ko.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
      : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
    delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
  ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('ja'),
    Locale('ko'),
    Locale('zh'),
    Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans'),
    Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant')
  ];

  /// No description provided for @untitled.
  ///
  /// In en, this message translates to:
  /// **'Untitled track'**
  String get untitled;

  /// No description provided for @homeTitle.
  ///
  /// In en, this message translates to:
  /// **'Bring music to your home screen'**
  String get homeTitle;

  /// No description provided for @homeDescription.
  ///
  /// In en, this message translates to:
  /// **'Browse Samsung Music’s playback queue and control playback from your home screen.'**
  String get homeDescription;

  /// No description provided for @permissionError.
  ///
  /// In en, this message translates to:
  /// **'Could not open settings. Enable notification access in your device settings.'**
  String get permissionError;

  /// No description provided for @permissionGranted.
  ///
  /// In en, this message translates to:
  /// **'Notification access enabled'**
  String get permissionGranted;

  /// No description provided for @firstPlayback.
  ///
  /// In en, this message translates to:
  /// **'To get started, play a song in Samsung Music.'**
  String get firstPlayback;

  /// No description provided for @pinFallback.
  ///
  /// In en, this message translates to:
  /// **'Touch and hold your home screen, then add MusicBridge from Widgets.'**
  String get pinFallback;

  /// No description provided for @addWidget.
  ///
  /// In en, this message translates to:
  /// **'Add to home screen'**
  String get addWidget;

  /// No description provided for @appearanceSettings.
  ///
  /// In en, this message translates to:
  /// **'Widget appearance settings'**
  String get appearanceSettings;

  /// No description provided for @permissionTitle.
  ///
  /// In en, this message translates to:
  /// **'Enable notification access'**
  String get permissionTitle;

  /// No description provided for @permissionDescription.
  ///
  /// In en, this message translates to:
  /// **'MusicBridge needs notification access to sync Samsung Music’s queue and playback status. Enable MusicBridge in system settings.'**
  String get permissionDescription;

  /// No description provided for @restrictedSettings.
  ///
  /// In en, this message translates to:
  /// **'If access is restricted, open Settings → Apps → MusicBridge and check the top-right menu for “Allow restricted settings”. Only allow this if you trust the installation source, then enable notification access.'**
  String get restrictedSettings;

  /// No description provided for @openSettings.
  ///
  /// In en, this message translates to:
  /// **'Open settings'**
  String get openSettings;

  /// No description provided for @appearanceTitle.
  ///
  /// In en, this message translates to:
  /// **'Widget appearance'**
  String get appearanceTitle;

  /// No description provided for @appearanceSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Customize the background to match your home screen.'**
  String get appearanceSubtitle;

  /// No description provided for @wallpaperHint.
  ///
  /// In en, this message translates to:
  /// **'Your wallpaper shows through the background. This preview is illustrative.'**
  String get wallpaperHint;

  /// No description provided for @backgroundColor.
  ///
  /// In en, this message translates to:
  /// **'Background color'**
  String get backgroundColor;

  /// No description provided for @applyAll.
  ///
  /// In en, this message translates to:
  /// **'Saving applies to all MusicBridge widgets.'**
  String get applyAll;

  /// No description provided for @retryAppearance.
  ///
  /// In en, this message translates to:
  /// **'Retry loading appearance'**
  String get retryAppearance;

  /// No description provided for @saved.
  ///
  /// In en, this message translates to:
  /// **'Saved. Home screen widgets updated.'**
  String get saved;

  /// No description provided for @saveError.
  ///
  /// In en, this message translates to:
  /// **'Could not save settings'**
  String get saveError;

  /// No description provided for @loadError.
  ///
  /// In en, this message translates to:
  /// **'Could not load settings. Please try again.'**
  String get loadError;

  /// No description provided for @followSystem.
  ///
  /// In en, this message translates to:
  /// **'Follow system'**
  String get followSystem;

  /// No description provided for @light.
  ///
  /// In en, this message translates to:
  /// **'Light'**
  String get light;

  /// No description provided for @dark.
  ///
  /// In en, this message translates to:
  /// **'Dark'**
  String get dark;

  /// No description provided for @opacity.
  ///
  /// In en, this message translates to:
  /// **'Opacity'**
  String get opacity;

  /// No description provided for @transparent.
  ///
  /// In en, this message translates to:
  /// **'Transparent'**
  String get transparent;

  /// No description provided for @opaque.
  ///
  /// In en, this message translates to:
  /// **'Opaque'**
  String get opaque;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @saving.
  ///
  /// In en, this message translates to:
  /// **'Saving…'**
  String get saving;

  /// No description provided for @save.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

  /// No description provided for @preview.
  ///
  /// In en, this message translates to:
  /// **'Appearance preview · sample background'**
  String get preview;

  /// No description provided for @demoQueue.
  ///
  /// In en, this message translates to:
  /// **'Sample songs · the widget shows your actual queue'**
  String get demoQueue;

  /// No description provided for @repeatOne.
  ///
  /// In en, this message translates to:
  /// **'Repeat one'**
  String get repeatOne;

  /// No description provided for @repeatAll.
  ///
  /// In en, this message translates to:
  /// **'Repeat all'**
  String get repeatAll;

  /// No description provided for @playOnce.
  ///
  /// In en, this message translates to:
  /// **'Play all once'**
  String get playOnce;

  /// No description provided for @repeatUnknown.
  ///
  /// In en, this message translates to:
  /// **'Playback mode unavailable'**
  String get repeatUnknown;

  /// No description provided for @shuffleOff.
  ///
  /// In en, this message translates to:
  /// **'Shuffle off'**
  String get shuffleOff;

  /// No description provided for @shuffleOn.
  ///
  /// In en, this message translates to:
  /// **'Shuffle on'**
  String get shuffleOn;

  /// No description provided for @shuffleUnknown.
  ///
  /// In en, this message translates to:
  /// **'Shuffle status unavailable'**
  String get shuffleUnknown;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'ja', 'ko', 'zh'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when language+script codes are specified.
  switch (locale.languageCode) {
    case 'zh':
      {
        switch (locale.scriptCode) {
          case 'Hans':
            return AppLocalizationsZhHans();
          case 'Hant':
            return AppLocalizationsZhHant();
        }
        break;
      }
  }

  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'ja':
      return AppLocalizationsJa();
    case 'ko':
      return AppLocalizationsKo();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
      'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
      'an issue with the localizations generation tool. Please file an issue '
      'on GitHub with a reproducible sample app and the gen-l10n configuration '
      'that was used.');
}
