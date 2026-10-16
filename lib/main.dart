import 'package:flutter/material.dart';
import 'app.dart';

// 保留原入口的公開名稱；新程式建議直接 import 對應檔案，來源更明確。
export 'app.dart';
export 'pages/bridge_page.dart';
export 'widgets/playback_mode_icons.dart';

/// Dart 程式入口，runApp 把最外層 Widget 交給 Flutter 建立畫面。
/// const 讓相同輸入的物件可以重用，不代表整個 App 的畫面不能變動。
void main() => runApp(const BridgeApp());
