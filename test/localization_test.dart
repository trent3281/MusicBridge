import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:music_bridge/app.dart';
import 'package:music_bridge/l10n/strings.dart';

void main() {
  test('resolves Chinese scripts and falls back to English', () {
    expect(resolveAppLocale([const Locale('zh', 'TW')], []),
        const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant'));
    expect(resolveAppLocale([const Locale('zh', 'CN')], []),
        const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans'));
    expect(
        resolveAppLocale([
          const Locale.fromSubtags(
              languageCode: 'zh', scriptCode: 'Hans', countryCode: 'TW')
        ], []),
        const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hans'));
    expect(
        resolveAppLocale([const Locale('ja', 'JP')], []), const Locale('ja'));
    expect(
        resolveAppLocale([const Locale('ko', 'KR')], []), const Locale('ko'));
    expect(resolveAppLocale([const Locale('fr')], []), const Locale('en'));
    expect(resolveAppLocale([const Locale('fr'), const Locale('zh', 'HK')], []),
        const Locale.fromSubtags(languageCode: 'zh', scriptCode: 'Hant'));
  });

  testWidgets(
      'system locale changes update home and settings without a language button',
      (tester) async {
    const channel = MethodChannel('music_bridge/media');
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      if (call.method == 'snapshot') return {'permission': true};
      if (call.method == 'appearance') return {'mode': 'dark', 'opacity': .35};
      return null;
    });
    addTearDown(() => TestDefaultBinaryMessengerBinding
        .instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null));
    addTearDown(tester.binding.platformDispatcher.clearLocalesTestValue);
    tester.binding.platformDispatcher.localesTestValue = [const Locale('en')];
    await tester.pumpWidget(const BridgeApp());
    await tester.pumpAndSettle();
    expect(find.text('Add to home screen'), findsOneWidget);
    tester.binding.platformDispatcher.localesTestValue = [
      const Locale('zh', 'TW')
    ];
    await tester.pumpAndSettle();
    expect(find.text('新增至桌面'), findsOneWidget);
    tester.binding.platformDispatcher.localesTestValue = [
      const Locale('zh', 'CN')
    ];
    await tester.pumpAndSettle();
    expect(find.text('添加到桌面'), findsOneWidget);
    await tester.tap(find.text('小组件外观设置'));
    await tester.pumpAndSettle();
    expect(find.text('保存'), findsOneWidget);
    tester.binding.platformDispatcher.localesTestValue = [const Locale('ja')];
    await tester.pumpAndSettle();
    expect(find.text('保存'), findsOneWidget);
    expect(find.text('システム設定に合わせる'), findsOneWidget);
    tester.binding.platformDispatcher.localesTestValue = [const Locale('ko')];
    await tester.pumpAndSettle();
    expect(find.text('저장'), findsOneWidget);
    expect(find.text('시스템 설정 따르기'), findsOneWidget);
    tester.binding.platformDispatcher.localesTestValue = [const Locale('fr')];
    await tester.pumpAndSettle();
    expect(find.text('Save'), findsOneWidget);
    expect(find.text('Follow system'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });
}
