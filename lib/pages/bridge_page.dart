import 'package:flutter/material.dart';
import '../controllers/bridge_controller.dart';
import '../widgets/appearance_controls.dart';
import '../widgets/music_preview.dart';

/// 頁面負責組合元件、監聽生命週期，以及需要 BuildContext 的提示訊息。
/// 真正資料與平台操作放在 BridgeController，不塞進 build()。
class BridgePage extends StatefulWidget {
  const BridgePage({super.key});
  @override
  State<BridgePage> createState() => _BridgePageState();
}

class _BridgePageState extends State<BridgePage> with WidgetsBindingObserver {
  late final BridgeController _controller;

  @override
  void initState() {
    super.initState();
    // initState 每個 State 只執行一次；不能把 Timer 建立在可能重複執行的 build。
    _controller = BridgeController()..start();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // 使用者從系統權限頁返回時，立即重新讀取連線資料。
    if (state == AppLifecycleState.resumed) _controller.refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller.dispose();
    super.dispose();
  }

  void _showMessage(String message) {
    // await 完成時使用者可能已離開頁面，此時不能再使用 context。
    if (!mounted) return;
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }

  Future<void> _save() async {
    try {
      await _controller.save();
      _showMessage('已儲存，桌面小工具已更新');
    } catch (e) {
      _showMessage('儲存失敗：$e');
    }
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
        // AnimatedBuilder 不只用於動畫：任何 Listenable 發出通知都會呼叫 builder。
        // 這裡監聽 ChangeNotifier，效果相當於原本在頁面呼叫 setState。
        animation: _controller,
        builder: (context, child) => Scaffold(
          appBar: AppBar(title: const Text('MusicBridge')),
          bottomNavigationBar: AppearanceActionBar(
              enabled: _controller.canEdit,
              saving: _controller.saving,
              onCancel: _controller.cancel,
              onSave: _save),
          // ListView 讓小螢幕也能向下捲動；SafeArea 由底部操作列處理系統手勢區。
          body: ListView(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
              children: [
                const Text('小工具外觀',
                    style:
                        TextStyle(fontSize: 30, fontWeight: FontWeight.bold)),
                const SizedBox(height: 6),
                const Text('調整背景，讓音樂融入你的桌面。',
                    style: TextStyle(color: Color(0xff666872))),
                const SizedBox(height: 22),
                AppearancePreviewCard(
                    settings: _controller.draft,
                    snapshot: _controller.snapshot,
                    artwork: _controller.artwork),
                const SizedBox(height: 10),
                const Text('背景會透出你自己的桌布；預覽圖片僅為示意。',
                    style: TextStyle(fontSize: 12, color: Color(0xff737680))),
                const SizedBox(height: 26),
                const Padding(
                    padding: EdgeInsets.only(left: 12, bottom: 10),
                    child: Text('背景色彩',
                        style: TextStyle(
                            fontSize: 16, fontWeight: FontWeight.bold))),
                BackgroundColorCard(
                    mode: _controller.draft.mode,
                    enabled: _controller.canEdit,
                    onChanged: _controller.changeMode),
                const SizedBox(height: 20),
                OpacityCard(
                    opacity: _controller.draft.opacity,
                    enabled: _controller.canEdit,
                    onChanged: _controller.changeOpacity),
                const Padding(
                    padding: EdgeInsets.fromLTRB(12, 10, 12, 20),
                    child: Text('儲存後套用至所有 MusicBridge 小工具。',
                        style: TextStyle(fontSize: 12, color: Colors.grey))),
                // collection-if：只有發生錯誤才把這兩個 Widget 加進 children。
                if (_controller.error != null) ...[
                  Text(_controller.error!,
                      style: const TextStyle(color: Colors.red)),
                  TextButton(
                      onPressed: _controller.loadAppearance,
                      child: const Text('重試載入外觀')),
                ],
              ]),
        ),
      );
}
