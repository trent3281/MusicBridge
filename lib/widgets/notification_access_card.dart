import 'package:flutter/material.dart';
import '../l10n/strings.dart';

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
              Text(context.strings.permissionTitle,
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              Text(context.strings.permissionDescription),
              const SizedBox(height: 8),
              Text(context.strings.restrictedSettings),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: onOpenSettings,
                icon: const Icon(Icons.settings_outlined),
                label: Text(context.strings.openSettings),
              ),
            ],
          ),
        ),
      );
}
