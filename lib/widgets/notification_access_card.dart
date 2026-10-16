import 'package:flutter/material.dart';

/// 只在通知存取權尚未開啟時顯示。授權後由頁面的快照更新自動移除，
/// 不將「曾按過設定」當成已授權，也不反覆強制跳轉系統設定。
class NotificationAccessCard extends StatelessWidget {
  const NotificationAccessCard({super.key, required this.onOpenSettings});

  final VoidCallback onOpenSettings;

  @override
  Widget build(BuildContext context) => Card(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('開啟通知存取權',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              const Text(
                  'MusicBridge 需要通知存取權，才能同步 Samsung Music 的歌曲清單與播放狀態。請在系統設定中開啟 MusicBridge。'),
              const SizedBox(height: 8),
              const Text(
                  '若系統禁止開啟，請先到「設定 → 應用程式 → MusicBridge」，查看右上角選單是否提供「允許受限制的設定」。僅在確認安裝來源可信時允許，再返回開啟通知存取權。'),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: onOpenSettings,
                icon: const Icon(Icons.settings_outlined),
                label: const Text('前往開啟'),
              ),
            ],
          ),
        ),
      );
}
