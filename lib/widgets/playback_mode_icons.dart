import 'package:flutter/material.dart';

/// Samsung 模式值對應文字；未知值不能當成關閉。
/// switch expression 回傳符合條件的結果，2 || 3 表示兩種值共用結果。
String playbackModeLabel(int mode) => switch (mode) {
      1 => '單曲循環',
      2 || 3 => '全部循環',
      0 => '全部播放一次',
      _ => '播放模式未公開',
    };

String shuffleModeLabel(int mode) => switch (mode) {
      0 => '隨機播放關閉',
      1 || 2 => '隨機播放開啟',
      _ => '隨機播放狀態未公開',
    };

/// Semantics 提供讀屏軟體可理解的文字，避免同一圖示被重複朗讀。
class ShuffleModeIcon extends StatelessWidget {
  const ShuffleModeIcon({super.key, required this.mode, this.size = 18.24});
  final int mode;
  final double size;
  @override
  Widget build(BuildContext context) => Semantics(
        label: shuffleModeLabel(mode),
        excludeSemantics: true,
        child: SizedBox(
            width: size,
            height: size,
            child: Stack(children: [
              Icon(Icons.shuffle, size: size),
              if (mode == 0)
                CustomPaint(
                    size: Size.square(size),
                    painter: _ShuffleSlash(
                        IconTheme.of(context).color ?? Colors.black)),
            ])),
      );
}

/// 用 CustomPainter 在隨機圖示上畫斜線；顏色變化時才要求重畫。
class _ShuffleSlash extends CustomPainter {
  const _ShuffleSlash(this.color);
  final Color color;
  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawLine(
        Offset(size.width * .125, size.height * .083),
        Offset(size.width * .875, size.height * .917),
        Paint()
          ..color = color
          ..strokeWidth = size.width * .075
          ..strokeCap = StrokeCap.round);
  }

  @override
  bool shouldRepaint(_ShuffleSlash oldDelegate) => color != oldDelegate.color;
}

/// Stack 疊放箭頭與 A 字，Positioned 指定各自位置。
/// if (...) ...[] 會把符合條件的一組 Widget 展開加入 children。
class PlaybackModeIcon extends StatelessWidget {
  const PlaybackModeIcon({super.key, required this.mode, this.size = 24});
  final int mode;
  final double size;
  @override
  Widget build(BuildContext context) => Semantics(
        label: playbackModeLabel(mode),
        excludeSemantics: true,
        child: SizedBox(
            width: size,
            height: size,
            child: Stack(
              alignment: Alignment.center,
              children: [
                if (mode == 0) ...[
                  Positioned(
                      bottom: 0,
                      child: Icon(Icons.arrow_right_alt, size: size)),
                  Positioned(
                      top: 0,
                      child: Text('A',
                          style: TextStyle(
                              fontSize: size * .5,
                              height: 1,
                              fontWeight: FontWeight.w600,
                              color: IconTheme.of(context).color))),
                ] else ...[
                  Icon(mode == 1 ? Icons.repeat_one : Icons.repeat, size: size),
                  if (mode == 2 || mode == 3)
                    Text('A',
                        style: TextStyle(
                            fontSize: size * .35,
                            height: 1,
                            fontWeight: FontWeight.bold,
                            color: IconTheme.of(context).color)),
                ],
              ],
            )),
      );
}
