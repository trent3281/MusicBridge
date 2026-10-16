import 'package:flutter/material.dart';
import 'pages/bridge_page.dart';

/// App 的最外層：設定全域主題和首頁，不放播放或設定儲存邏輯。
/// StatelessWidget 的輸出由輸入決定；「無狀態」不代表永遠不會重新 build。
class BridgeApp extends StatelessWidget {
  const BridgeApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
            useMaterial3: true,
            colorScheme:
                ColorScheme.fromSeed(seedColor: const Color(0xff387af3)),
            scaffoldBackgroundColor: const Color(0xfff0f1f4),
            appBarTheme: const AppBarTheme(backgroundColor: Color(0xfff0f1f4))),
        home: const BridgePage(),
      );
}
