import 'package:flutter/widgets.dart';
import 'app_localizations.dart';

/// 所有畫面透過 Localizations 訂閱系統語言，切換語言時會自動重建。
extension LocalizedContext on BuildContext {
  AppLocalizations get strings =>
      AppLocalizations.of(this) ?? lookupAppLocalizations(const Locale('zh'));
}

/// Android 的中文語系可能只有地區碼，明確對應字形，避免 zh-TW 誤選簡體。
Locale resolveAppLocale(List<Locale>? preferred, Iterable<Locale> supported) {
  for (final locale in preferred ?? <Locale>[]) {
    if (locale.languageCode == 'zh') {
      final traditional = locale.scriptCode == 'Hant' ||
          (locale.scriptCode != 'Hans' &&
              !['CN', 'SG'].contains(locale.countryCode));
      return Locale.fromSubtags(
          languageCode: 'zh', scriptCode: traditional ? 'Hant' : 'Hans');
    }
    if (['en', 'ja', 'ko'].contains(locale.languageCode)) {
      return Locale(locale.languageCode);
    }
  }
  return const Locale('en');
}
