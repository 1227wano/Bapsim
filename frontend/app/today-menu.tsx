import { Ionicons, MaterialIcons } from '@expo/vector-icons';
import { router } from 'expo-router';
import React, { useEffect, useMemo, useState } from 'react';
import {
  Dimensions,
  Image,
  Platform,
  SafeAreaView,
  ScrollView,
  StatusBar,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { styles } from '../screens/TodayMenuScreen.styles';

const { width } = Dimensions.get('window');

// 타입 정의: API 매핑 후 화면에서 사용하는 카드 구조
type MenuItem = { name: string; calories?: number; isHealthy?: boolean };
type MenuCard = {
  title: string;
  mainDish: { name: string; image: any; calories?: number };
  items: MenuItem[];
  allergy?: string[];
};

const TodayMenuScreen = () => {
  // ===== 데모용 식당/메뉴 데이터 (추후 API 연동 예정) =====
  // 로그인 연동 전까지 식당은 1로 고정 (하드코딩 목록 제거)

  // 날짜 유틸
  const startOfWeek = (date: Date) => {
    const d = new Date(date);
    const day = d.getDay(); // 0: Sun ~ 6: Sat
    const diff = (day === 0 ? -6 : 1) - day; // 월요일 시작
    d.setDate(d.getDate() + diff);
    d.setHours(0, 0, 0, 0);
    return d;
  };
  const addDays = (date: Date, days: number) => {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    return d;
  };
  const formatKoreanDate = (date: Date) => {
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekday = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
    return `${month}월 ${day}일 ${weekday}요일`;
  };
  const formatRange = (start: Date, end: Date) => {
    const s = `${start.getMonth() + 1}/${start.getDate()}`;
    const e = `${end.getMonth() + 1}/${end.getDate()}`;
    return `${s} - ${e}`;
  };
  const formatIsoDate = (date: Date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  // 선택 상태
  const [selectedRestaurantId, setSelectedRestaurantId] = useState(1);
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [isRestaurantOpen, setIsRestaurantOpen] = useState(false);

  const weekStart = useMemo(() => startOfWeek(selectedDate), [selectedDate]);
  const weekEnd = useMemo(() => addDays(weekStart, 6), [weekStart]);

  const [totalCalories, setTotalCalories] = useState(0);
  const [fetchedMenus, setFetchedMenus] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  // API 응답 -> 화면 카드 데이터로 매핑
  const apiMenuCards = useMemo<MenuCard[] | null>(() => {
    if (!fetchedMenus || !Array.isArray(fetchedMenus)) return null;
    const sampleImage = require('../assets/images/food_sample.jpg');
    const dateStr = formatIsoDate(selectedDate);
    // 식당 1번 데이터 중 선택 날짜 + 시그니처 메뉴만 표시
    const filtered = fetchedMenus.filter((m: any) => String(m?.menuDate) === dateStr && m?.isSignature === true);
    return filtered.map((m: any) => ({
      title: m?.mealType ?? '',
      mainDish: {
        // 백엔드 직렬화 케이스 대응: menuName(카멜), menu_name(스네이크), content(구 필드)
        name: m?.food?.menuName ?? m?.food?.menu_name ?? '',
        image: sampleImage,
        calories: Number(m?.food?.kcal) || undefined,
      },
      menuNo: m?.menuNo,
      mealType: m?.mealType,
      resNo: m?.resNo || m?.restaurant?.resNo,
      items: [],
      allergy: m?.food?.allergyInfo ? String(m.food.allergyInfo).split(',').map((s: string) => s.trim()).filter(Boolean) : undefined,
    }));
  }, [fetchedMenus, selectedDate]);

  // 실제 렌더에 사용할 카드 데이터 (API 결과 없으면 빈 배열)
  const menuCardsForRender = useMemo<MenuCard[]>(() => {
    return apiMenuCards && apiMenuCards.length > 0 ? apiMenuCards : [];
  }, [apiMenuCards]);

  // 즐겨찾기 토글
  // 즐겨찾기 제거 요구사항에 따라 관련 상태/핸들러 제거

  // 총 칼로리 계산 (실제로는 API에서 받아올 수 있음)
  useEffect(() => {
    const total = menuCardsForRender.reduce((sum, set) => {
      const main = set.mainDish?.calories ?? 0;
      const items = (set.items || []).reduce((s, i) => s + (i.calories || 0), 0);
      return sum + main + items;
    }, 0);
    setTotalCalories(total);
  }, [menuCardsForRender]);

  // 뒤로가기
  const handleBack = () => {
    router.back();
  };

  // 품절 여부 상태 및 확인 핸들러
  const [isSoldOut, setIsSoldOut] = useState(false);
  const [checkingSoldOut, setCheckingSoldOut] = useState(false);

  const handleCheckSoldOut = () => {
    if (checkingSoldOut) return;
    setCheckingSoldOut(true);
    setTimeout(() => {
      const result = Math.random() < 0.4; // 데모용 확률
      setIsSoldOut(result);
      setCheckingSoldOut(false);
    }, 700);
  };

  // 날짜 네비게이션 핸들러 (하루 단위)
  const goPrevDay = () => setSelectedDate(d => addDays(d, -1));
  const goNextDay = () => setSelectedDate(d => addDays(d, 1));

  // 드롭다운에 표시할 식당 목록: 현재 불러온 메뉴의 restaurant 정보를 사용(다건 확장 대비)
  const restaurants = useMemo(() => {
    if (!Array.isArray(fetchedMenus) || fetchedMenus.length === 0) {
      return [{ id: selectedRestaurantId, name: '식당 1' }];
    }
    // 중복 제거
    const map = new Map<number, string>();
    for (const m of fetchedMenus) {
      const id = Number(m?.resNo || m?.restaurant?.resNo || selectedRestaurantId);
      const name = String(m?.restaurant?.resName || '식당 1');
      if (!map.has(id)) map.set(id, name);
    }
    return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
  }, [fetchedMenus, selectedRestaurantId]);

  const selectedRestaurant = useMemo(() => restaurants.find(r => r.id === selectedRestaurantId), [restaurants, selectedRestaurantId]);

  // ===== /api 호출: 선택 식당 데이터 조회, 날짜는 클라이언트에서 필터 =====
  useEffect(() => {
    const controller = new AbortController();
    const fetchMenus = async () => {
      try {
        setIsLoading(true);
        const url = `http://localhost:8082/api/menus/restaurant/${selectedRestaurantId}`;
        const res = await fetch(url, { signal: controller.signal });
  
        if (!res.ok) {
          // HTML 오류 페이지가 올 경우도 여기서 잡힘
          const text = await res.text();
          console.log('[today-menu] /api/menus failed, HTML response:', text);
          throw new Error(`HTTP ${res.status}`);
        }
  
        const json = await res.json();
        console.log('[today-menu] GET /api/menus', { url, status: res.status, data: json });
        setFetchedMenus(json);
  
      } catch (err: any) {
        if (err?.name !== 'AbortError') {
          console.log('[today-menu] /api/menus request failed', err);
        }
      }
      finally {
        setIsLoading(false);
      }
    };
    fetchMenus();
    return () => controller.abort();
  }, [selectedRestaurantId]);
  

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#f8f9fa" />
      
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={handleBack} style={styles.backButton} activeOpacity={0.7}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>오늘의 메뉴</Text>
        <View style={styles.headerActions}>
          <TouchableOpacity style={styles.shareButton} activeOpacity={0.7} onPress={() => router.push('/chatbot')}>
            <Ionicons name="chatbubble-ellipses-outline" size={24} color="#666" />
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView 
        style={styles.content} 
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        {/* 날짜 네비게이션: 단일 바 */}
        <View style={styles.dateContainer}>
          <TouchableOpacity onPress={goPrevDay} activeOpacity={0.7} disabled={isLoading} style={isLoading ? { opacity: 0.4 } : undefined}>
            <Ionicons name="chevron-back" size={22} color="#1BB1E7" />
          </TouchableOpacity>
          <Text style={styles.dateText}>{formatKoreanDate(selectedDate)}</Text>
          <TouchableOpacity onPress={goNextDay} activeOpacity={0.7} disabled={isLoading} style={isLoading ? { opacity: 0.4 } : undefined}>
            <Ionicons name="chevron-forward" size={22} color="#1BB1E7" />
          </TouchableOpacity>
        </View>

        {/* 장소/식당 선택 */}
        <View style={[styles.dateContainer, isRestaurantOpen ? { marginBottom: 8 } : null]}>
          <MaterialIcons name="place" size={20} color="#1BB1E7" />
          <TouchableOpacity
            style={{ flexDirection: 'row', alignItems: 'center', marginLeft: 10, justifyContent: 'center' }}
            onPress={() => restaurants.length > 1 && setIsRestaurantOpen(o => !o)}
            activeOpacity={0.7}
          >
            <Text style={styles.dateText}>
              {selectedRestaurant?.name || '식당 선택'}
            </Text>
            {restaurants.length > 1 && (
              <Ionicons
                name={isRestaurantOpen ? 'chevron-up' : 'chevron-down'}
                size={18}
                color="#777"
                style={{ marginLeft: 6 }}
              />
            )}
          </TouchableOpacity>
        </View>
        {isRestaurantOpen && restaurants.length > 1 && (
          <View style={{ backgroundColor: '#fff', borderRadius: 12, marginTop: -6, marginBottom: 12, overflow: 'hidden',
            ...Platform.select({ ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.1, shadowRadius: 8 }, android: { elevation: 6 } }) }}>
            {restaurants.map(r => (
              <TouchableOpacity
                key={r.id}
                onPress={() => { setSelectedRestaurantId(r.id); setIsRestaurantOpen(false); }}
                style={{ paddingVertical: 10, paddingHorizontal: 16, borderBottomWidth: 1, borderBottomColor: '#f0f0f0' }}
                activeOpacity={0.7}
              >
                <Text style={{ color: r.id === selectedRestaurantId ? '#1BB1E7' : '#333', fontWeight: r.id === selectedRestaurantId ? '700' : '500' }}>
                  {r.name}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        )}

        {/* 2열 카드: 각 카테고리 메인 이미지 표시 (이미지 클릭 시 상세로 이동) */}
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', marginBottom: 16 }}>
          {menuCardsForRender.length === 0 && !isLoading && (
            <View style={{ width: '100%', alignItems: 'center', paddingVertical: 24 }}>
              <Text style={{ color: '#666' }}>선택한 날짜의 메뉴가 없습니다.</Text>
            </View>
          )}
          {menuCardsForRender.map((set: any, idx) => {
            const calorieText = set.mainDish?.calories ? `${set.mainDish.calories.toLocaleString()}Kcal` : '';
            const subText = (set.items || []).map((i: MenuItem) => i.name).join(', ');
            return (
              <View key={`card-${idx}`} style={{ width: (width - 40 - 12) / 2, marginBottom: 18 }}>
                {/* 이미지 카드 */}
                <View style={{ width: '100%', height: (width - 40 - 12) / 2, borderRadius: 16, overflow: 'hidden', backgroundColor: '#000',
                  ...Platform.select({ ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.15, shadowRadius: 8 }, android: { elevation: 4 } }) }}>
                  <TouchableOpacity
                    activeOpacity={0.85}
                    onPress={() => (router as any).push({
                      pathname: '/today-menu-detail',
                      params: {
                        cat: set.title,
                        data: JSON.stringify(set),
                        date: formatKoreanDate(selectedDate),
                        place: selectedRestaurant?.name || '',
                        soldout: String(isSoldOut),
                        menuNo: String(set.menuNo ?? ''),
                        resNo: String(set.resNo ?? ''),
                        isoDate: String(formatIsoDate(selectedDate)),
                        mealType: String(set.mealType ?? ''),
                      },
                    })}
                  >
                    <Image source={set.mainDish?.image} style={{ width: '100%', height: (width - 40 - 12) / 2, resizeMode: 'cover' }} />
                  </TouchableOpacity>
                  {isSoldOut && (
                    <View style={styles.soldOutOverlay}>
                      <Text style={styles.soldOutText}>품절</Text>
                    </View>
                  )}
                </View>
                {/* 메타 정보 */}
                <View style={{ marginTop: 10 }}>
                  <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                    <Text style={{ color: '#333', fontWeight: '700' }}> {String.fromCharCode(65 + idx)}:{set.title}</Text>
                    {!!calorieText && <Text style={{ color: '#666', fontWeight: '600' }}>{calorieText}</Text>}
                  </View>
                  <Text numberOfLines={1} style={{ color: '#111', fontWeight: '800', fontSize: 20 }}>{set.mainDish?.name}</Text>
                  {!!subText && (
                    <Text numberOfLines={1} style={{ color: '#9aa0a6', marginTop: 8 }}>{subText}</Text>
                  )}
                </View>
              </View>
            );
          })}
        </View>

        {/* 카테고리 상세 리스트는 상세 화면에서 표시 */}

        {/* 영양/알레르기 정보는 상세 화면에서 개별 표시 */}

        {/* 추가 기능 버튼들 */}
        <View style={styles.actionButtons}>
          <TouchableOpacity style={styles.actionButton} activeOpacity={0.7}>
            <Ionicons name="restaurant-outline" size={20} color="#666" />
            <Text style={styles.actionButtonText}>식당 위치</Text>
          </TouchableOpacity>
        </View>

        
      </ScrollView>
    </SafeAreaView>
  );
};

export default TodayMenuScreen;
