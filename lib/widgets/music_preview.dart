import 'dart:typed_data';
import 'package:flutter/material.dart';
import '../l10n/strings.dart';
import '../models/appearance_settings.dart';
import '../models/playback_snapshot.dart';
import 'playback_mode_icons.dart';

const _fontScale = AppearanceSettings.fontScale;
const _headerExtent = 88.0;

/// 設定頁中的示意桌布 + 小工具預覽。
/// 注意：這是 Flutter 畫面；真正桌面小工具是 Android XML + RemoteViews。
/// 這裡使用漸層，並不讀取手機桌布，也不需要桌布讀取權限。
class AppearancePreviewCard extends StatelessWidget {
  const AppearancePreviewCard(
      {super.key,
      required this.settings,
      required this.snapshot,
      this.artwork});
  final AppearanceSettings settings;
  final PlaybackSnapshot snapshot;
  final Uint8List? artwork;
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(32),
            gradient: const LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Color(0xffa5c9c7),
                  Color(0xff6e819c),
                  Color(0xffd2b6a4)
                ])),
        child: Column(children: [
          Align(
              alignment: Alignment.centerRight,
              child: Text(context.strings.preview,
                  style: TextStyle(fontSize: 11, color: Colors.white))),
          const SizedBox(height: 14),
          MusicPreview(
              settings: settings, snapshot: snapshot, artwork: artwork),
        ]),
      );
}

/// 純顯示元件：收到資料就畫出預覽，不輪詢、不存設定、不控制播放。
class MusicPreview extends StatelessWidget {
  const MusicPreview(
      {super.key,
      required this.settings,
      required this.snapshot,
      this.artwork});
  final AppearanceSettings settings;
  final PlaybackSnapshot snapshot;
  final Uint8List? artwork;
  @override
  Widget build(BuildContext context) {
    final palette = _PreviewPalette(settings.isDark(
        systemIsDark:
            MediaQuery.platformBrightnessOf(context) == Brightness.dark));
    final demo = snapshot.queue.isEmpty;
    // const 示範資料只會出現在設定預覽，絕不會送去控制 Samsung Music。
    final rows = demo
        ? const [
            QueueSong(title: 'メフィスト', artist: '女王蜂'),
            QueueSong(title: '花', artist: '藤井風'),
            QueueSong(title: '冰解', artist: 'れん'),
            QueueSong(title: '雪庇', artist: 'れん'),
          ]
        : snapshot.queue.take(4).toList();
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
          color: palette.background.withValues(alpha: settings.opacity),
          borderRadius: BorderRadius.circular(28)),
      child: IconTheme(
          data: IconThemeData(color: palette.foreground, size: 26),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            _PreviewHeader(
                snapshot: snapshot, artwork: artwork, palette: palette),
            const SizedBox(height: 14),
            for (final song in rows)
              _PreviewSongRow(
                  song: song,
                  palette: palette,
                  active: demo
                      ? identical(song, rows.first)
                      : song.id == snapshot.activeId),
            if (demo)
              Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Text(context.strings.demoQueue,
                      style:
                          TextStyle(fontSize: 10, color: palette.secondary))),
          ])),
    );
  }
}

/// 專輯圖、右上歌曲資訊、右下控制列共用同一個高度。
class _PreviewHeader extends StatelessWidget {
  const _PreviewHeader(
      {required this.snapshot, required this.palette, this.artwork});
  final PlaybackSnapshot snapshot;
  final _PreviewPalette palette;
  final Uint8List? artwork;
  @override
  Widget build(BuildContext context) =>
      LayoutBuilder(builder: (context, constraints) {
        final width = constraints.maxWidth + 32;
        final compact = width < 340;
        final height = compact ? 76.0 : _headerExtent;
        final cover = width >= 260 ? height : 0.0;
        final gap = cover == 0
            ? 0.0
            : compact
                ? 8.0
                : 12.0;
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (cover > 0) ...[
              _PreviewArtwork(bytes: artwork, extent: cover),
              SizedBox(width: gap),
            ],
            // Row 裡的 Expanded 取得扣除封面後的剩餘寬度，長歌名才知道要在哪裡截斷。
            Expanded(
                child: SizedBox(
                    height: height,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // 設定預覽是靜態示意；桌面跑馬燈設定在 music_widget.xml 的 title。
                        Text(snapshot.title ?? 'リナリア',
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                                fontSize: 18 * _fontScale,
                                height: 1.2,
                                color: palette.foreground,
                                fontWeight: FontWeight.w600)),
                        Text(snapshot.artist ?? 'まるりとりゅうが',
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                                fontSize: 14 * _fontScale,
                                height: 1.2,
                                color: palette.secondary)),
                        // Spacer 消耗剩下的垂直空間，讓控制列貼齊封面底部。
                        const Spacer(),
                        SizedBox(
                            height: compact ? 40 : 48,
                            child: Row(children: [
                              Expanded(
                                  child: Center(
                                      child: ShuffleModeIcon(
                                          mode: snapshot.shuffle,
                                          size: 16.72))),
                              const Expanded(
                                  child: Center(
                                      child: Icon(Icons.skip_previous,
                                          size: 16.72))),
                              Expanded(
                                  child: Center(
                                      child: Icon(
                                          snapshot.playing
                                              ? Icons.pause
                                              : Icons.play_arrow,
                                          size: 21.28))),
                              const Expanded(
                                  child: Center(
                                      child:
                                          Icon(Icons.skip_next, size: 16.72))),
                              Expanded(
                                  child: Center(
                                      child: PlaybackModeIcon(
                                          mode: snapshot.repeat, size: 16.72))),
                            ])),
                      ],
                    ))),
          ],
        );
      });
}

class _PreviewArtwork extends StatelessWidget {
  const _PreviewArtwork({this.bytes, required this.extent});
  final Uint8List? bytes;
  final double extent;

  Widget _placeholder(BuildContext context) => Container(
      decoration: BoxDecoration(
          color: (IconTheme.of(context).color ?? Colors.white)
              .withValues(alpha: 40 / 255),
          borderRadius: BorderRadius.circular(extent * 24 / 256)),
      child: Center(child: Icon(Icons.music_note, size: extent * .375)));

  @override
  Widget build(BuildContext context) => SizedBox(
      width: extent,
      height: extent,
      child: bytes == null
          ? _placeholder(context)
          : ClipRRect(
              borderRadius: BorderRadius.circular(6),
              child: Image.memory(bytes!,
                  fit: BoxFit.cover,
                  gaplessPlayback: false,
                  // 圖片損壞也使用音符佔位，不讓整個預覽顯示錯誤畫面。
                  errorBuilder: (context, error, stackTrace) =>
                      _placeholder(context))));
}

class _PreviewSongRow extends StatelessWidget {
  const _PreviewSongRow(
      {required this.song, required this.active, required this.palette});
  final QueueSong song;
  final bool active;
  final _PreviewPalette palette;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 5),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          // 沒有播放箭頭時仍保留相同寬度，所有列的歌名才會對齊。
          SizedBox(
              width: 13,
              height: 22,
              child: active
                  ? Icon(Icons.play_arrow, size: 15.6, color: palette.accent)
                  : null),
          const SizedBox(width: 15),
          Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                Text(song.title.isEmpty ? context.strings.untitled : song.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 17 * _fontScale,
                        color: active ? palette.accent : palette.foreground)),
                const SizedBox(height: 2),
                Text(song.artist,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14 * _fontScale,
                        color: active ? palette.accent : palette.secondary)),
              ])),
        ]),
      );
}

/// 顏色集中在一處，確保預覽標題和歌曲列使用同一套深／淺色規則。
class _PreviewPalette {
  const _PreviewPalette(this.dark);
  final bool dark;
  Color get background =>
      dark ? const Color(0xff181818) : const Color(0xfff8f8f8);
  Color get foreground => dark ? Colors.white : const Color(0xff19191c);
  Color get secondary =>
      dark ? const Color(0xffcdcdd2) : const Color(0xff242428);
  Color get accent => dark ? const Color(0xffaacdff) : const Color(0xff245cb4);
}
