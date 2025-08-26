import { StyleSheet, Platform, StatusBar } from 'react-native';

export const styles = StyleSheet.create({
  // ===== 메인 컨테이너 스타일 =====
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingTop: Platform.OS === 'android' ? StatusBar.currentHeight : 0,
  },

  // ===== 헤더 영역 스타일 =====
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    // iOS와 Android 플랫폼별 그림자 효과
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 3,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  headerTopBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 10,
    paddingBottom: 10,
  },
  headerLogo: {
    width: 100,
    height: 50,
  },
  chatIconButton: {
    padding: 6,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    textAlign: 'left',
    marginHorizontal: 20,
    marginTop: 16,
    marginBottom: 8,
  },

  // ===== 메인 콘텐츠 영역 스타일 =====
  menuSection: {
    backgroundColor: '#fff',
    borderRadius: 15,
    padding: 20,
    marginTop: 20,
    // 플랫폼별 그림자 효과
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  content: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 20,
  },

  // ===== 메뉴 그리드 레이아웃 스타일 =====
  menuRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 25,
  },
  menuButton: {
    width: '22%',
    alignItems: 'center',
  },

  // ===== 메뉴 아이콘 컨테이너 스타일 =====
  iconContainer: {
    position: 'relative',
    width: 60,
    height: 60,
    backgroundColor: '#fff',
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
    // 플랫폼별 그림자 효과
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  // 특별한 아이콘 (밥먹뽀먹) 스타일
  specialIcon: {
    backgroundColor: '#FFF8E1',
    borderWidth: 2,
    borderColor: '#FFD54F',
    // 특별한 아이콘의 강화된 그림자 효과
    ...Platform.select({
      ios: {
        shadowColor: '#FFD54F',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
      },
      android: {
        elevation: 5,
      },
    }),
  },

  // ===== 아이콘 관련 스타일 =====
  iconText: {
    fontSize: 24,
  },
  iconImage: {
    width: 50,
    height: 50,
  },

  // ===== NEW 배지 스타일 =====
  newBadge: {
    position: 'absolute',
    top: -5,
    right: -5,
    backgroundColor: '#FF69B4',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 10,
    minWidth: 20,
    alignItems: 'center',
  },
  newBadgeText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: 'bold',
  },

  // ===== 메뉴 텍스트 스타일 =====
  menuTitle: {
    fontSize: 12,
    fontWeight: '600',
    color: '#333',
    textAlign: 'center',
    lineHeight: 16,
  },
  menuSubtitle: {
    fontSize: 10,
    color: '#999',
    textAlign: 'center',
    marginTop: 2,
  },

  // ===== 학생 정보 섹션 스타일 =====
  studentSection: {
    marginTop: 0,
  },
  studentCard: {
    backgroundColor: '#1BB1E7',
    borderRadius: 20,
    padding: 20,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.15,
        shadowRadius: 8,
      },
      android: { elevation: 5 },
    }),
  },
  studentCardLabel: {
    color: '#FFE9D7',
    fontSize: 13,
    marginBottom: 8,
    fontWeight: '700',
  },
  studentTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
    marginBottom: 16,
  },
  avatarCircle: {
    width: 75,
    height: 75,
    borderRadius: 32,
    backgroundColor: 'rgba(255,255,255,0.15)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 20,
  },
  studentTexts: {
  },
  studentDeptMajor: {
    color: '#FFE9D7',
    fontSize: 14,
    marginBottom: 4,
  },
  studentId: {
    color: '#FFE9D7',
    fontSize: 14,
    marginBottom: 4,
  },
  studentName: {
    color: '#fff',
    fontSize: 22,
    fontWeight: 'bold',
  },
  studentBadgeRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 10,
  },
  studentBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255,255,255,0.25)',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginRight: 10,
  },
  studentBadgeText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '700',
    marginLeft: 6,
  },

  // ===== 하단 탭 바 스타일 =====
  bottomTab: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderTopWidth: 1,
    borderTopColor: '#eee',
    // 플랫폼별 그림자 효과
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: -1 },
        shadowOpacity: 0.1,
        shadowRadius: 3,
      },
      android: {
        elevation: 5,
      },
    }),
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 5,
  },
  tabText: {
    fontSize: 11,
    fontWeight: '500',
    color: '#999',
    marginTop: 4,
  },

  // ===== 작은 NEW 배지 스타일 (하단 탭용) =====
  newBadgeSmall: {
    position: 'absolute',
    top: -5,
    right: '35%',
    backgroundColor: '#FF69B4',
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 8,
    zIndex: 1,
    minWidth: 20,
    alignItems: 'center',
  },
  newBadgeTextSmall: {
    color: '#fff',
    fontSize: 8,
    fontWeight: 'bold',
  },
});

