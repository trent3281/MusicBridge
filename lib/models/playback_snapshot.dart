/// Android 回傳的單一歌曲。queueId 只對「這次媒體連線」有效，
/// 點歌要傳 mediaId/uri 等歌曲身分，不能沿用快取中的舊 queueId。
class QueueSong {
  const QueueSong(
      {this.id,
      this.mediaId,
      this.uri,
      this.title = '未命名歌曲',
      this.artist = ''});

  final int? id;
  final String? mediaId;
  final String? uri;
  final String title;
  final String artist;

  factory QueueSong.fromMap(Map<dynamic, dynamic> map) => QueueSong(
        id: (map['id'] as num?)?.toInt(),
        mediaId: map['mediaId'] as String?,
        uri: map['uri'] as String?,
        title: map['title'] as String? ?? '未命名歌曲',
        artist: map['artist'] as String? ?? '',
      );

  Map<String, Object?> toSelection() => {
        'mediaId': mediaId,
        'uri': uri,
        'title': title,
        'artist': artist,
      };
}

/// Android PlaybackState 的動作是 bit flags（位元旗標）。
/// 例如 flags & next != 0，表示回報的能力包含「下一首」。
/// 用名稱集中定義，可以避免畫面中到處出現看不懂的數字。
abstract final class MediaAction {
  static const pause = 2;
  static const play = 4;
  static const previous = 16;
  static const next = 32;
  static const playPause = 512;
  static const queueItem = 4096;
  static const repeat = 262144;
  static const shuffle = 2097152;
}

/// 播放器在某一刻回報的完整資料，並非 Flutter 自己推測的播放狀態。
/// 把原生 Map 的型別轉換放在這裡，畫面就能使用 snapshot.playing 等屬性。
class PlaybackSnapshot {
  PlaybackSnapshot(Map<String, dynamic> data)
      : _data = Map.unmodifiable(data),
        queue = List.unmodifiable((data['queue'] as List? ?? [])
            .map((item) => QueueSong.fromMap(item as Map)));

  final Map<String, dynamic> _data;
  final List<QueueSong> queue;
  String? get title => _data['title'] as String?;
  String? get artist => _data['artist'] as String?;
  bool get connected => _data['connected'] == true;
  bool get cached => _data['cached'] == true;
  bool get playing => _data['playing'] == true;
  int get actions => (_data['actions'] as num?)?.toInt() ?? 0;
  int? get activeId => (_data['activeId'] as num?)?.toInt();
  int get repeat => (_data['repeat'] as num?)?.toInt() ?? -1;
  int get shuffle => (_data['shuffle'] as num?)?.toInt() ?? -1;
  int get artworkRevision => (_data['artworkRevision'] as num?)?.toInt() ?? 0;
  String get message => _data['message'] as String? ?? '';
  String get shuffleStatus => _data['shuffleStatus'] as String? ?? '';
  int get queueRevision => (_data['queueRevision'] as num?)?.toInt() ?? 0;
  String get lastProbe => _data['lastProbe'] as String? ?? '';
  bool get sessionReady => _data['sessionReady'] == true;
  List<String> get customActions =>
      (_data['customActions'] as List? ?? []).map((e) => e.toString()).toList();
  List<String> get mediaEvents =>
      (_data['mediaEvents'] as List? ?? []).map((e) => e.toString()).toList();

  /// 離線快取只供顯示；不能把儲存過的動作旗標當成目前仍可操作。
  bool supports(int flag) => connected && actions & flag != 0;
}
