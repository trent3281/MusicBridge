import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:music_bridge/pages/bridge_page.dart';
import 'package:music_bridge/widgets/playback_mode_icons.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const channel = MethodChannel('music_bridge/media');
  final saved = <Map<dynamic, dynamic>>[];
  setUp(() {
    saved.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      if (call.method == 'appearance') {
        return {'mode': 'light', 'opacity': 0.6};
      }
      if (call.method == 'snapshot') return {'queue': [], 'actions': 0};
      if (call.method == 'saveAppearance') {
        saved.add(call.arguments as Map<dynamic, dynamic>);
      }
      return null;
    });
  });
  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });
  testWidgets('loads saved opacity; cancel keeps persisted appearance intact',
      (tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const MaterialApp(home: BridgePage()));
    await tester.pumpAndSettle();
    expect(tester.widget<Slider>(find.byKey(const ValueKey('opacity'))).value,
        0.6);
    await tester.ensureVisible(find.text('深色'));
    await tester.tap(find.text('深色'));
    await tester.pumpAndSettle();
    await tester.pump();
    await tester.tap(find.text('取消'));
    await tester.pumpAndSettle();
    expect(saved, isEmpty);
    expect(find.byKey(const ValueKey('fontScale')), findsNothing);
    expect(find.text('字體大小'), findsNothing);
    await tester.tap(find.text('儲存'));
    await tester.pumpAndSettle();
    expect(saved.single, {'mode': 'light', 'opacity': 0.6});
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('saves color and fully transparent background together',
      (tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const MaterialApp(home: BridgePage()));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('深色'));
    await tester.tap(find.text('深色'));
    tester.widget<Slider>(find.byKey(const ValueKey('opacity'))).onChanged!(0);
    await tester.pump();
    await tester.tap(find.text('儲存'));
    await tester.pumpAndSettle();
    expect(saved.single, {'mode': 'dark', 'opacity': 0.0});
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('fixed font fits a narrow phone preview', (tester) async {
    tester.view.physicalSize = const Size(360, 800);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const MaterialApp(home: BridgePage()));
    await tester.pumpAndSettle();
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(find.text('外觀預覽 · 示意背景'), -250);
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    // 窄版預覽縮小封面，優先保留五個控制鍵的空間。
    expect(tester.widget<Icon>(find.byIcon(Icons.music_note)).size,
        lessThan(88 * .375));
    expect(find.byIcon(Icons.skip_previous), findsOneWidget);
    expect(find.byIcon(Icons.skip_next), findsOneWidget);
    expect(find.byIcon(Icons.play_arrow), findsWidgets);
    expect(tester.widget<Text>(find.text('リナリア')).style!.fontSize, 18 * .9);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('only active queue title artist and triangle use accent',
      (tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      if (call.method == 'appearance') return {'mode': 'light', 'opacity': .6};
      if (call.method == 'snapshot')
        return {
          'connected': true,
          'activeId': 42,
          'repeat': 1,
          'shuffle': 0,
          'title': 'Header',
          'artist': 'Header Artist',
          'queue': [
            {'id': 42, 'title': 'Selected Title', 'artist': 'Selected Artist'},
            {'id': 43, 'title': 'Other Title', 'artist': 'Other Artist'},
          ],
        };
      return null;
    });
    await tester.pumpWidget(const MaterialApp(home: BridgePage()));
    await tester.pumpAndSettle();
    const accent = Color(0xff245cb4);
    expect(
        tester.widget<Text>(find.text('Selected Title')).style!.color, accent);
    expect(
        tester.widget<Text>(find.text('Selected Artist')).style!.color, accent);
    expect(tester.widget<Text>(find.text('Other Title')).style!.color,
        const Color(0xff19191c));
    expect(
        find.byWidgetPredicate((widget) =>
            widget is Icon &&
            widget.icon == Icons.play_arrow &&
            widget.color == accent),
        findsOneWidget);
    final repeatIcon = find.byType(PlaybackModeIcon).first;
    final repeatTheme = IconTheme.of(tester.element(repeatIcon));
    expect(repeatTheme.color, const Color(0xff19191c));
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('playback modes use distinct labels and symbols', (tester) async {
    final semantics = tester.ensureSemantics();
    for (final mode in [0, 1, 2]) {
      await tester.pumpWidget(
          MaterialApp(home: Scaffold(body: PlaybackModeIcon(mode: mode))));
      expect(find.bySemanticsLabel(playbackModeLabel(mode)), findsOneWidget);
      if (mode == 0) expect(find.byIcon(Icons.arrow_right_alt), findsOneWidget);
      if (mode == 1) expect(find.byIcon(Icons.repeat_one), findsOneWidget);
      if (mode == 2) {
        expect(find.byIcon(Icons.repeat), findsOneWidget);
        expect(find.text('A'), findsOneWidget);
      }
    }
    semantics.dispose();
  });
}
