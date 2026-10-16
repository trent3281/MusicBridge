import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:music_bridge/app.dart';

void main() {
  const channel = MethodChannel('music_bridge/media');
  testWidgets('authorization gates pinning and settings stays separate',
      (tester) async {
    var permission = false;
    final calls = <String>[];
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      calls.add(call.method);
      if (call.method == 'snapshot') return {'permission': permission};
      if (call.method == 'appearance') return {'mode': 'dark', 'opacity': 0.35};
      return null;
    });
    addTearDown(() => TestDefaultBinaryMessengerBinding
        .instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null));
    await tester.pumpWidget(const BridgeApp());
    await tester.pumpAndSettle();
    expect(find.text('前往開啟'), findsOneWidget);
    expect(find.text('新增至桌面'), findsNothing);
    expect(find.text('儲存'), findsNothing);
    await tester.tap(find.text('前往開啟'));
    await tester.pump();
    expect(calls, contains('permission'));
    permission = true;
    await tester.pump(const Duration(seconds: 2));
    await tester.pumpAndSettle();
    expect(find.text('前往開啟'), findsNothing);
    await tester.tap(find.text('新增至桌面'));
    await tester.pump();
    expect(calls, contains('pin'));
    await tester.tap(find.text('小工具外觀設定'));
    await tester.pumpAndSettle();
    expect(find.text('儲存'), findsOneWidget);
    expect(find.text('新增至桌面'), findsNothing);
    await tester.pumpWidget(const SizedBox());
  });
}
