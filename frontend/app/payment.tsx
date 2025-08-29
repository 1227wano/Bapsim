import React, { useEffect, useMemo, useState } from 'react';
import { SafeAreaView, View, Text, ScrollView, TouchableOpacity, StatusBar, Platform, Image, TextInput } from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { Ionicons, MaterialIcons } from '@expo/vector-icons';
import { styles as baseStyles } from '../screens/TodayMenuScreen.styles';

type PriceItem = { price?: number; description?: string; mealType?: string; kind?: string };

export default function PaymentScreen() {
  const params = useLocalSearchParams();
  const title = typeof params.title === 'string' ? params.title : '';
  const mainName = typeof params.mainName === 'string' ? params.mainName : '';
  const kcal = typeof params.kcal === 'string' ? Number(params.kcal) : 0;
  const isoDate = typeof params.isoDate === 'string' ? params.isoDate : '';
  const mealType = typeof params.mealType === 'string' ? params.mealType : '';
  const place = typeof params.place === 'string' ? params.place : '';
  const pointsParam = typeof params.points === 'string' ? Number(params.points) : undefined;

  const [selectedMethod, setSelectedMethod] = useState<'card' | 'account'>('account');
  const [points, setPoints] = useState<number>(pointsParam ?? 0);
  const [usePoints, setUsePoints] = useState<number>(0);
  const [prices, setPrices] = useState<PriceItem[] | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // 주문 금액 조회: 식사타입/날짜 기준 가격 목록 호출 후 첫 항목 선택
  const orderPrice = useMemo(() => {
    if (!Array.isArray(prices) || prices.length === 0) return undefined;
    const match = prices.find(p => !p.mealType || p.mealType === mealType) || prices[0];
    return match?.price;
  }, [prices, mealType]);

  useEffect(() => {
    let aborted = false;
    const fetchPrices = async () => {
      if (!mealType || !isoDate) return;
      try {
        setIsLoading(true);
        const url = `http://localhost:8082/api/menus/prices/meal-type/${encodeURIComponent(mealType)}/${encodeURIComponent(isoDate)}`;
        const res = await fetch(url);
        if (!res.ok) return;
        const json = await res.json();
        if (!aborted) setPrices(Array.isArray(json) ? json : []);
      } finally {
        setIsLoading(false);
      }
    };
    fetchPrices();
    return () => {
      aborted = true;
    };
  }, [mealType, isoDate]);

  const expectedReward = useMemo(() => {
    if (selectedMethod !== 'account' || !orderPrice) return 0;
    return Math.floor(orderPrice * 0.02);
  }, [selectedMethod, orderPrice]);

  const finalPrice = useMemo(() => {
    if (!orderPrice) return 0;
    return Math.max(0, orderPrice - usePoints);
  }, [orderPrice, usePoints]);

  return (
    <SafeAreaView style={baseStyles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#f8f9fa" />

      {/* 헤더 */}
      <View style={baseStyles.header}>
        <TouchableOpacity onPress={() => router.back()} style={baseStyles.backButton} activeOpacity={0.7}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={baseStyles.headerTitle}>결제하기</Text>
        <View style={{ width: 24 }} />
      </View>

      <ScrollView style={baseStyles.content} contentContainerStyle={baseStyles.scrollContent}>
        {/* 1. 주문내역 */}
        <View style={[baseStyles.sideDishesSection, { marginTop: 8 }]}> 
          <Text style={baseStyles.sectionTitle}>주문내역</Text>
          <View style={baseStyles.sideDishItem}>
            <View style={baseStyles.dishInfo}>
              <View style={{ flexDirection: 'row', alignItems: 'flex-start' }}>
                <Image source={require('../assets/images/food_sample.jpg')} style={{ width: 60, height: 60, borderRadius: 8, marginRight: 12 }} />
                <View style={{ flex: 1 }}>
                  <Text style={{ color: '#666', fontSize: 14, marginBottom: 4 }}>{place}</Text>
                  <Text style={baseStyles.dishName}>{mainName || title}</Text>
                </View>
              </View>
            </View>
          </View>
        </View>

        {/* 2. 보유 포인트 */}
        <View style={baseStyles.nutritionSection}>
          <Text style={baseStyles.sectionTitle}>보유 포인트</Text>
          <View style={baseStyles.nutritionCard}>
            <View style={baseStyles.totalCaloriesContainer}>
              <Text style={baseStyles.totalCaloriesLabel}>내 포인트</Text>
              <Text style={baseStyles.totalCaloriesValue}>{points.toLocaleString()} P</Text>
            </View>
            <View style={{ marginTop: 12, paddingHorizontal: 16 }}>
              <Text style={{ color: '#666', fontSize: 14, marginBottom: 8 }}>사용할 포인트</Text>
              <TextInput
                style={{ borderWidth: 1, borderColor: '#ddd', borderRadius: 8, padding: 12, fontSize: 16 }}
                placeholder="0"
                keyboardType="numeric"
                value={String(usePoints)}
                onChangeText={(text) => setUsePoints(Number(text) || 0)}
              />
              <Text style={{ color: '#999', fontSize: 12, marginTop: 4 }}>최대 {points.toLocaleString()}P까지 사용 가능</Text>
            </View>
          </View>
        </View>

        {/* 3. 결제수단 */}
        <View style={baseStyles.sideDishesSection}>
          <Text style={baseStyles.sectionTitle}>결제수단</Text>
          <View style={[baseStyles.sideDishItem, { alignItems: 'flex-start' }]}> 
            <TouchableOpacity onPress={() => setSelectedMethod('card')} activeOpacity={0.7} style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Ionicons name={selectedMethod === 'card' ? 'radio-button-on' : 'radio-button-off'} size={18} color="#1BB1E7" />
              <Text style={{ marginLeft: 8, color: '#333' }}>신용/체크카드</Text>
            </TouchableOpacity>
          </View>
          <View style={[baseStyles.sideDishItem, { alignItems: 'flex-start' }]}> 
            <TouchableOpacity onPress={() => setSelectedMethod('account')} activeOpacity={0.7} style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Ionicons name={selectedMethod === 'account' ? 'radio-button-on' : 'radio-button-off'} size={18} color="#1BB1E7" />
              <Text style={{ marginLeft: 8, color: '#333' }}>계좌 간편결제</Text>
              <Text style={{ marginLeft: 10, color: '#4CAF50', fontWeight: '700' }}>신한 계좌 결제 시 2% 적립</Text>
            </TouchableOpacity>
            {selectedMethod === 'account' && (
              <View style={[baseStyles.accountItem]}>
                <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 8}}>
                  <Text style={{ color: '#333', fontWeight: '600' }}>신한은행 </Text>
                  <Text style={{ color: '#333', fontWeight: '600' }}>110-123456789</Text>
                </View>
              </View>
            )}
          </View>
        </View>

        {/* 4. 주문 금액 */}
        <View style={baseStyles.nutritionSection}>
          <Text style={baseStyles.sectionTitle}>주문 금액</Text>
          <View style={baseStyles.nutritionCard}>
            <View style={baseStyles.totalCaloriesContainer}>
              <Text style={baseStyles.totalCaloriesLabelSmall}>상품 금액</Text>
              <Text style={baseStyles.totalCaloriesValueSmall}>{orderPrice !== undefined ? `${orderPrice.toLocaleString()}원` : (isLoading ? '조회 중...' : '가격 정보 없음')}</Text>
            </View>
            <View style={baseStyles.totalCaloriesContainer}>
              <Text style={baseStyles.totalCaloriesLabelSmall}>포인트 사용</Text>
              <Text style={baseStyles.totalCaloriesValueSmall}>{usePoints.toLocaleString()} P</Text>
            </View>
            <View style={baseStyles.totalCaloriesContainer}>
              <Text style={baseStyles.totalCaloriesLabel}>최종 결제 금액</Text>
              <Text style={baseStyles.totalCaloriesValue}>{finalPrice.toLocaleString()}원</Text>
            </View>
            {expectedReward > 0 && (
              <View>
                <Text style={{ color: '#4CAF50', fontSize: 12, textAlign: 'right' }}>
                  예상 적립 {expectedReward.toLocaleString()} P
                </Text>
              </View>
            )}
          </View>
        </View>

        {/* 하단 버튼 */}
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 15 }}>
          <TouchableOpacity style={baseStyles.actionButton} activeOpacity={0.7} onPress={() => router.back()}>
            <Ionicons name="close" size={20} color="#666" />
            <Text style={baseStyles.actionButtonText}>취소</Text>
          </TouchableOpacity>
          <TouchableOpacity style={baseStyles.actionButton} activeOpacity={0.7}>
            <Ionicons name="checkmark-circle-outline" size={20} color="#666" />
            <Text style={baseStyles.actionButtonText}>결제하기</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}


