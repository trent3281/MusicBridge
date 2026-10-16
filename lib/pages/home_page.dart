import 'package:flutter/material.dart';
import '../controllers/bridge_controller.dart';
import '../widgets/notification_access_card.dart';

/// App 圖示開啟的首頁：管理授權與加入桌面，外觀編輯留在獨立路由。
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  late final BridgeController _controller;

  @override
  void initState() {
    super.initState();
    _controller = BridgeController()..start();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _controller.refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller.dispose();
    super.dispose();
  }

  Future<void> _run(Future<void> Function() action, String error) async {
    try {
      await action();
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(error)));
    }
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
        animation: _controller,
        builder: (context, _) => Scaffold(
          appBar: AppBar(title: const Text('MusicBridge')),
          body: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              const Icon(Icons.library_music_outlined, size: 64),
              const SizedBox(height: 24),
              Text('讓音樂回到桌面',
                  style: Theme.of(context).textTheme.headlineMedium),
              const SizedBox(height: 12),
              const Text('同步 Samsung Music 的播放佇列，直接在桌面選曲與控制播放。'),
              const SizedBox(height: 24),
              if (_controller.snapshot.permission == null)
                const Center(child: CircularProgressIndicator())
              else if (_controller.snapshot.permission == false)
                NotificationAccessCard(
                  onOpenSettings: () => _run(
                      _controller.openNotificationSettings,
                      '無法開啟系統設定，請從手機設定開啟通知存取權。'),
                )
              else ...[
                const ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: Icon(Icons.check_circle_outline),
                  title: Text('通知存取權已開啟'),
                  subtitle: Text('首次使用請先在 Samsung Music 播放歌曲。'),
                ),
                const SizedBox(height: 12),
                FilledButton.icon(
                  onPressed: () => _run(_controller.requestWidgetPin,
                      '請長按桌面，從小工具清單加入 MusicBridge。'),
                  icon: const Icon(Icons.add_to_home_screen),
                  label: const Text('新增至桌面'),
                ),
              ],
              const SizedBox(height: 24),
              OutlinedButton.icon(
                onPressed: () => Navigator.of(context).pushNamed('/settings'),
                icon: const Icon(Icons.palette_outlined),
                label: const Text('小工具外觀設定'),
              ),
            ],
          ),
        ),
      );
}
