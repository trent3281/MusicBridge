import 'package:flutter/material.dart';
import '../l10n/strings.dart';

/// 只負責外觀的白色圓角容器。child 由呼叫端決定，方便組合不同設定。
class SettingsCard extends StatelessWidget {
  const SettingsCard({super.key, required this.child});
  final Widget child;
  @override
  Widget build(BuildContext context) => Container(
      decoration: BoxDecoration(
          color: Colors.white, borderRadius: BorderRadius.circular(28)),
      child: child);
}

/// StatelessWidget 不保存選取狀態：父層傳入 mode，點擊後透過 callback
/// 回報新值，再由控制器更新資料。這就是「資料向下、事件向上」。
class BackgroundColorCard extends StatelessWidget {
  const BackgroundColorCard(
      {super.key,
      required this.mode,
      required this.enabled,
      required this.onChanged});
  final String mode;
  final bool enabled;
  final ValueChanged<String> onChanged;
  @override
  Widget build(BuildContext context) => SettingsCard(
          child: Column(children: [
        // record（紀錄）把設定值與顯示文字配成一組；$1/$2 分別取第一/二欄。
        for (final choice in [
          ('system', context.strings.followSystem),
          ('light', context.strings.light),
          ('dark', context.strings.dark)
        ])
          Material(
              type: MaterialType.transparency,
              child: ListTile(
                enabled: enabled,
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 3),
                leading: Icon(
                    mode == choice.$1
                        ? Icons.radio_button_checked
                        : Icons.radio_button_off,
                    color: mode == choice.$1
                        ? const Color(0xff387af3)
                        : Colors.grey),
                title: Text(choice.$2, style: const TextStyle(fontSize: 18)),
                onTap: enabled ? () => onChanged(choice.$1) : null,
              )),
      ]));
}

class OpacityCard extends StatelessWidget {
  const OpacityCard(
      {super.key,
      required this.opacity,
      required this.enabled,
      required this.onChanged});
  final double opacity;
  final bool enabled;
  final ValueChanged<double> onChanged;
  @override
  Widget build(BuildContext context) => SettingsCard(
          child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 18, 20, 8),
        child: Column(children: [
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text(context.strings.opacity,
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            Text('${(opacity * 100).round()}%',
                style: const TextStyle(color: Color(0xff387af3))),
          ]),
          Slider(
              key: const ValueKey('opacity'),
              value: opacity,
              min: 0,
              max: 1,
              divisions: 10,
              label: '${(opacity * 100).round()}%',
              // onChanged 設為 null 是 Flutter 元件停用的標準方式。
              onChanged: enabled ? onChanged : null),
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text(context.strings.transparent,
                style: TextStyle(fontSize: 11, color: Colors.grey)),
            Text(context.strings.opaque,
                style: TextStyle(fontSize: 11, color: Colors.grey)),
          ]),
          const SizedBox(height: 8),
        ]),
      ));
}

/// 底部操作列只有按鈕，並不知道資料如何保存。
class AppearanceActionBar extends StatelessWidget {
  const AppearanceActionBar(
      {super.key,
      required this.enabled,
      required this.saving,
      required this.onCancel,
      required this.onSave});
  final bool enabled;
  final bool saving;
  final VoidCallback onCancel;
  final VoidCallback onSave;
  @override
  Widget build(BuildContext context) => SafeArea(
          child: Padding(
        padding: const EdgeInsets.fromLTRB(24, 12, 24, 16),
        child: Row(children: [
          Expanded(
              child: TextButton(
                  onPressed: enabled ? onCancel : null,
                  child: Text(context.strings.cancel,
                      style: TextStyle(
                          fontSize: 18, fontWeight: FontWeight.bold)))),
          Expanded(
              child: FilledButton(
                  onPressed: enabled ? onSave : null,
                  child: Text(
                      saving ? context.strings.saving : context.strings.save,
                      style: const TextStyle(
                          fontSize: 18, fontWeight: FontWeight.bold)))),
        ]),
      ));
}
