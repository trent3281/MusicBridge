// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Korean (`ko`).
class AppLocalizationsKo extends AppLocalizations {
  AppLocalizationsKo([String locale = 'ko']) : super(locale);

  @override
  String get untitled => '제목 없음';

  @override
  String get homeTitle => '홈 화면에서 즐기는 음악';

  @override
  String get homeDescription =>
      'Samsung Music의 재생 대기열을 확인하고 홈 화면에서 곡을 선택하거나 재생을 제어하세요.';

  @override
  String get permissionError => '설정을 열 수 없습니다. 기기 설정에서 알림 접근을 허용해 주세요.';

  @override
  String get permissionGranted => '알림 접근이 허용되었습니다';

  @override
  String get firstPlayback => '처음 사용하려면 Samsung Music에서 곡을 재생해 주세요.';

  @override
  String get pinFallback => '홈 화면을 길게 누르고 위젯 목록에서 MusicBridge를 추가하세요.';

  @override
  String get addWidget => '홈 화면에 추가';

  @override
  String get appearanceSettings => '위젯 모양 설정';

  @override
  String get permissionTitle => '알림 접근 허용';

  @override
  String get permissionDescription =>
      'Samsung Music의 재생 대기열과 재생 상태를 동기화하려면 알림 접근 권한이 필요합니다. 시스템 설정에서 MusicBridge를 허용해 주세요.';

  @override
  String get restrictedSettings =>
      '설정이 제한된 경우 \'설정 → 애플리케이션 → MusicBridge\'의 오른쪽 위 메뉴에서 \'제한된 설정 허용\'을 확인하세요. 설치 출처를 신뢰할 수 있는 경우에만 허용한 후 알림 접근을 켜 주세요.';

  @override
  String get openSettings => '설정 열기';

  @override
  String get appearanceTitle => '위젯 모양';

  @override
  String get appearanceSubtitle => '홈 화면에 어울리도록 배경을 조정하세요.';

  @override
  String get wallpaperHint => '실제로는 기기의 배경화면이 비쳐 보입니다. 미리보기 배경은 예시입니다.';

  @override
  String get backgroundColor => '배경색';

  @override
  String get applyAll => '저장하면 모든 MusicBridge 위젯에 적용됩니다.';

  @override
  String get retryAppearance => '모양 설정 다시 불러오기';

  @override
  String get saved => '저장하고 위젯을 업데이트했습니다';

  @override
  String get saveError => '설정을 저장할 수 없습니다';

  @override
  String get loadError => '설정을 불러올 수 없습니다. 다시 시도해 주세요.';

  @override
  String get followSystem => '시스템 설정 따르기';

  @override
  String get light => '라이트';

  @override
  String get dark => '다크';

  @override
  String get opacity => '불투명도';

  @override
  String get transparent => '완전 투명';

  @override
  String get opaque => '완전 불투명';

  @override
  String get cancel => '취소';

  @override
  String get saving => '저장 중…';

  @override
  String get save => '저장';

  @override
  String get preview => '모양 미리보기 · 예시 배경';

  @override
  String get demoQueue => '예시 곡 · 위젯에는 실제 재생 대기열이 표시됩니다';

  @override
  String get repeatOne => '한 곡 반복';

  @override
  String get repeatAll => '전체 반복';

  @override
  String get playOnce => '전체 한 번 재생';

  @override
  String get repeatUnknown => '재생 모드를 확인할 수 없습니다';

  @override
  String get shuffleOff => '랜덤 재생 꺼짐';

  @override
  String get shuffleOn => '랜덤 재생 켜짐';

  @override
  String get shuffleUnknown => '랜덤 재생 상태를 확인할 수 없습니다';
}
