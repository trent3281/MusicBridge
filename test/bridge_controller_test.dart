import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:music_bridge/controllers/bridge_controller.dart';
import 'package:music_bridge/models/appearance_settings.dart';
import 'package:music_bridge/models/playback_snapshot.dart';
import 'package:music_bridge/services/music_bridge_api.dart';

// 假服務不需要手機或 MethodChannel，專門檢查控制器的非同步與設定行為。
class FakeMusicBridgeApi extends MusicBridgeApi {
  AppearanceSettings saved =
      const AppearanceSettings(mode: 'light', opacity: .6);
  bool failSave = false;
  int snapshotCalls = 0;
  int artworkCalls = 0;
  int saveCalls = 0;
  Completer<PlaybackSnapshot>? pendingSnapshot;
  PlaybackSnapshot next = PlaybackSnapshot({'artworkRevision': 1});

  @override
  Future<AppearanceSettings> loadAppearance() async => saved;
  @override
  Future<void> saveAppearance(AppearanceSettings settings) async {
    saveCalls++;
    if (failSave) throw StateError('save failed');
    saved = settings;
  }

  @override
  Future<PlaybackSnapshot> loadSnapshot() {
    snapshotCalls++;
    return pendingSnapshot?.future ?? Future.value(next);
  }

  @override
  Future<Uint8List?> loadArtwork() async {
    artworkCalls++;
    return null;
  }
}

void main() {
  test('cancel changes only draft; failed save preserves saved settings',
      () async {
    final api = FakeMusicBridgeApi();
    final controller = BridgeController(api: api);
    addTearDown(controller.dispose);
    await controller.loadAppearance();
    controller.changeMode('dark');
    controller.changeOpacity(0);
    controller.cancel();
    expect(controller.draft.mode, 'light');
    expect(controller.draft.opacity, .6);
    expect(api.saveCalls, 0);

    controller.changeOpacity(.2);
    api.failSave = true;
    await expectLater(controller.save(), throwsStateError);
    expect(controller.saving, isFalse);
    expect(controller.canEdit, isTrue);
    controller.cancel();
    expect(controller.draft.opacity, .6);
  });

  test('successful save becomes the next cancel target', () async {
    final api = FakeMusicBridgeApi();
    final controller = BridgeController(api: api);
    addTearDown(controller.dispose);
    await controller.loadAppearance();
    controller.changeOpacity(0);
    await controller.save();
    controller.changeOpacity(1);
    controller.cancel();
    expect(controller.draft.opacity, 0);
    expect(api.saved.opacity, 0);
  });

  test('overlapping refresh is skipped; unchanged artwork is not fetched twice',
      () async {
    final api = FakeMusicBridgeApi()
      ..pendingSnapshot = Completer<PlaybackSnapshot>();
    final controller = BridgeController(api: api);
    addTearDown(controller.dispose);
    final first = controller.refresh();
    await controller.refresh();
    expect(api.snapshotCalls, 1);
    api.pendingSnapshot!.complete(api.next);
    await first;
    api.pendingSnapshot = null;
    await controller.refresh();
    expect(api.artworkCalls, 1);
    api.next = PlaybackSnapshot({'artworkRevision': 2});
    await controller.refresh();
    expect(api.artworkCalls, 2);
  });

  test('disposing during a platform request ignores its late result', () async {
    final api = FakeMusicBridgeApi()
      ..pendingSnapshot = Completer<PlaybackSnapshot>();
    final controller = BridgeController(api: api);
    var notifications = 0;
    controller.addListener(() => notifications++);
    final pending = controller.refresh();
    controller.dispose();
    api.pendingSnapshot!.complete(PlaybackSnapshot({'title': 'Late song'}));
    await pending;
    expect(notifications, 0);
    expect(api.artworkCalls, 0);
    expect(controller.snapshot.title, isNull);
  });

  test(
      'cached queue cannot grant controls or carry an old queue ID into selection',
      () {
    final snapshot = PlaybackSnapshot({
      'connected': false,
      'cached': true,
      'actions': MediaAction.queueItem,
      'queue': [
        {'id': 99, 'mediaId': 'stable', 'title': 'Song', 'artist': 'Artist'}
      ],
    });
    expect(snapshot.supports(MediaAction.queueItem), isFalse);
    expect(snapshot.queue.single.toSelection(), {
      'mediaId': 'stable',
      'uri': null,
      'title': 'Song',
      'artist': 'Artist',
    });
  });
}
