import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, SafeAreaView, StatusBar, ScrollView, TouchableOpacity, Image, StyleSheet, Dimensions } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Config } from '../constants/Config';
import { styles } from '../screens/TodayMenuScreen.styles';

export default function TodayMenuDetail() {
  const params = useLocalSearchParams();
  const title = typeof params.cat === 'string' ? params.cat : '상세 메뉴';
  const place = typeof params.place === 'string' ? params.place : '';
  const date = typeof params.date === 'string' ? params.date : '';
  const soldout = typeof params.soldout === 'string' ? params.soldout === 'true' : false;
  const raw = typeof params.data === 'string' ? params.data : undefined;
  const menuId = typeof params.menuId === 'string' ? params.menuId : '';
  const menuNo = typeof params.menuNo === 'string' ? params.menuNo : '';
  const resNo = typeof params.resNo === 'string' ? params.resNo : '';
  const isoDate = typeof params.isoDate === 'string' ? params.isoDate : '';
  const mealType = typeof params.mealType === 'string' ? params.mealType : '';
  const kind = typeof params.kind === 'string' ? params.kind : '';

  const data = useMemo(() => {
    try {
      return raw ? JSON.parse(raw) : undefined;
    } catch {
      return undefined;
    }
  }, [raw]);

  // 샘플 데이터 설정
  const [foodDetail, setFoodDetail] = useState<any>({
    menuName: '장조림버터밥',
    kcal: 423,
    allergyInfo: '밀,계란'
  });

  const [relatedMenus, setRelatedMenus] = useState<any[]>([
    {
      food: {
        menuName: '미니냉소바',
        kcal: 379,
        allergyInfo: '밀'
      }
    },
    {
      food: {
        menuName: '오징어링튀김',
        kcal: 177,
        allergyInfo: '밀,계란'
      }
    },
    {
      food: {
        menuName: '핫도그&셀탕&계잠',
        kcal: 147,
        allergyInfo: '밀,계란'
      }
    },
    {
      food: {
        menuName: '샐러드',
        kcal: 11,
        allergyInfo: ''
      }
    },
    {
      food: {
        menuName: '드레싱',
        kcal: 44,
        allergyInfo: '계란'
      }
    },
    {
      food: {
        menuName: '배추김치',
        kcal: 14,
        allergyInfo: ''
      }
    },
    {
      food: {
        menuName: '유자차',
        kcal: 52,
        allergyInfo: ''
      }
    }
  ]);

  const items = data?.items || [];
  const mainDish = data?.mainDish || {
    name: '장조림버터밥',
    calories: 423,
    image: require('../assets/images/food_sample9.jpg')
  };

  const allergies = (data?.allergy || []).concat(
    foodDetail?.allergyInfo ? String(foodDetail.allergyInfo).split(',').map((s: string) => s.trim()).filter(Boolean) : []
  );

  const combinedAllergies = useMemo(() => {
    const set = new Set<string>();
    allergies.forEach((a: string) => {
      if (a) set.add(a);
    });
    relatedMenus.forEach((m: any) => {
      const arr = m?.food?.allergyInfo ? String(m.food.allergyInfo).split(',').map((s: string) => s.trim()).filter(Boolean) : [];
      arr.forEach((a: string) => {
        if (a) set.add(a);
      });
    });
    return Array.from(set);
  }, [allergies, relatedMenus]);

  const totalCalories = useMemo(() => {
    const main = Number(foodDetail?.kcal || mainDish?.calories || 0);
    // 메인 메뉴는 relatedMenus에서 제외했으므로 단순 합산
    const relatedSum = relatedMenus.reduce((sum: number, m: any) => sum + Number(m?.food?.kcal || 0), 0);
    return main + relatedSum;
  }, [foodDetail, mainDish, relatedMenus]);

  const { width } = Dimensions.get('window');

  useEffect(() => {
    let aborted = false;
    const fetchFood = async () => {
      if (!menuId) return;
      try {
        const url = `${Config.API_BASE_URL}/api/menus/${menuId}/food`;
        const res = await fetch(url);
        if (!res.ok) return;
        const json = await res.json();
        if (!aborted) setFoodDetail(json);
      } catch {}
    };

    // 샘플 데이터 사용을 위해 API 호출 주석 처리
    // fetchFood();

    return () => {
      aborted = true;
    };
  }, [menuId]);

  // 같은 날짜_타입_ prefix로 묶인 메뉴들 가져오기: 식당별 전체 메뉴에서 필터
  useEffect(() => {
    let aborted = false;
    const fetchRestaurantMenus = async () => {
      if (!resNo) return;
      try {
        const url = `${Config.API_BASE_URL}/api/menus/restaurant/${resNo}`;
        const res = await fetch(url);
        if (!res.ok) return;
        const list = await res.json();
        const all = Array.isArray(list) ? list : [];
        const sameDayType = all
          .filter((m: any) => String(m?.menuDate).slice(0,10) === isoDate && String(m?.mealType) === mealType)
          .filter((m: any) => String(m?.menuId) !== String(menuId)); // 메인 메뉴 제외

        if (!aborted) setRelatedMenus(sameDayType);
      } catch {}
    };

    // 샘플 데이터 사용을 위해 API 호출 주석 처리
    // fetchRestaurantMenus();

    return () => {
      aborted = true;
    };
  }, [resNo, isoDate, mealType]);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#f8f9fa" />
      
      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={styles.backButton}
          activeOpacity={0.7}
        >
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>{title}</Text>
        <View style={{ width: 24 }} />
      </View>

      <ScrollView style={styles.content} contentContainerStyle={styles.scrollContent}>
        {/* 메인 이미지 */}
        <View style={{
          width: '100%',
          height: width * 0.6,
          borderRadius: 16,
          overflow: 'hidden',
          backgroundColor: '#000',
          marginBottom: 16
        }}>
          {mainDish?.image && (
            <Image
              source={mainDish.image }
              style={{
                width: '100%',
                height: '100%',
                resizeMode: 'cover'
              }}
            />
          )}
        </View>

        {/* 상세 리스트 */}
        <View style={styles.sideDishesSection}>
          <Text style={styles.sectionTitle}>메인</Text>
          <View style={styles.sideDishItem}>
            <View style={styles.dishInfo}>
              <View style={styles.dishNameContainer}>
                <Text style={styles.dishName}>
                  {foodDetail?.menuName || foodDetail?.menu_name || mainDish?.name}
                </Text>
                {Array.isArray(allergies) && allergies.length === 0 && (
                  <View style={styles.healthyBadge}>
                    <Ionicons name="leaf-outline" size={12} color="#4CAF50" />
                    <Text style={styles.healthyText}>건강</Text>
                  </View>
                )}
                {soldout && (
                  <Text style={[styles.dishName, { color: '#E53935' }]}> (품절)</Text>
                )}
              </View>
              {!!(foodDetail?.kcal || mainDish?.calories) && (
                <Text style={styles.dishCalories}>
                  {(foodDetail?.kcal || mainDish?.calories)} Kcal
                </Text>
              )}
            </View>
          </View>
        </View>

        <View style={styles.sideDishesSection}>
          <Text style={styles.sectionTitle}>함께하는 메뉴</Text>
          {relatedMenus.map((m: any, index: number) => {
            const allergies = m?.food?.allergyInfo ? String(m.food.allergyInfo).split(',').map((s: string) => s.trim()).filter(Boolean) : [];
            return (
              <View key={index} style={styles.sideDishItem}>
                <View style={styles.dishInfo}>
                  <View style={styles.dishNameContainer}>
                    <Text style={styles.dishName}>
                      {m?.food?.menuName || m?.food?.menu_name || m?.food?.content}
                    </Text>
                  </View>
                  {!!m?.food?.kcal && (
                    <Text style={styles.dishCalories}>{m.food.kcal} Kcal</Text>
                  )}
                </View>
                {allergies.length > 0 && (
                  <View style={{
                    flexDirection: 'row',
                    flexWrap: 'wrap',
                    gap: 6,
                    marginTop: 6
                  }}>
                  </View>
                )}
              </View>
            );
          })}
        </View>

        {/* 영양 정보 */}
        <View style={styles.nutritionSection}>
          <Text style={styles.sectionTitle}>상세영양정보</Text>
          <View style={styles.nutritionCard}>
            <View style={styles.totalCaloriesContainer}>
              <Text style={styles.totalCaloriesLabel}>총 칼로리</Text>
              <Text style={styles.totalCaloriesValue}>{totalCalories} Kcal</Text>
            </View>
            <View style={styles.calorieProgressBar}>
              <View style={styles.progressBarBackground}>
                <View
                  style={[
                    styles.progressBarFill,
                    { width: `${Math.min((totalCalories / 2000) * 100, 100)}%` }
                  ]}
                />
              </View>
              <Text style={styles.progressText}>
                일일 권장량의 {Math.round((totalCalories / 2000) * 100)}%
              </Text>
            </View>
          </View>
        </View>

        {/* 알레르기 정보 (메인+연관 메뉴 통합, 중복 제거) */}
        <View style={styles.allergySection}>
          <Text style={styles.sectionTitle}>알레르기 정보</Text>
          <View style={styles.allergyContainer}>
            {combinedAllergies.map((allergy: string, index: number) => (
              <View key={index} style={styles.allergyBadge}>
                <Ionicons name="warning-outline" size={16} color="#FF9800" />
                <Text style={styles.allergyText}>{allergy}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* 하단 결제하기 버튼 */}
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 15 }}>
          <TouchableOpacity
            style={styles.actionButton}
            activeOpacity={0.7}
            onPress={() => router.push({
              pathname: '/payment',
              params: {
                mainName: foodDetail?.menuName || foodDetail?.menu_name || mainDish?.name || '',
                isoDate,
                mealType,
                kind, // kind 파라미터 전달
                place,
                menuNo, // menuNo 파라미터 전달
                points: '10000', // TODO: 로그인 연동 시 사용자 포인트로 교체
              }
            })}
          >
            <Ionicons name="card-outline" size={20} color="#666" />
            <Text style={styles.actionButtonText}>결제하기</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}