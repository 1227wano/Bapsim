import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Platform,
  TouchableOpacity,
  Image,
  Dimensions,
} from 'react-native';
import { Ionicons, MaterialIcons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { styles } from '../screens/TodayMenuScreen.styles';

const { width } = Dimensions.get('window');

const TodayMenuScreen = () => {
  // 추후 API 데이터로 교체될 상태
  const [menuData, setMenuData] = useState({
    date: '8월 25일 월요일',
    place: '학생회관 2F',
    mainDish: {
      name: '김치나베돈가스',
      image: require('../assets/images/food_sample.jpg'), // 임시 이미지
      calories: 447,
      isFavorite: false,
    },
    sideDishes: [
      { name: '쌀밥&후리가케', calories: 185, isHealthy: true, image: require('../assets/images/food_sample.jpg') },
      { name: '으깬단호박샐러드', calories: 114, isHealthy: true, image: require('../assets/images/food_sample.jpg') },
      { name: '오복지무침', calories: 5, isHealthy: true, image: require('../assets/images/main_icon/global_icon.png') },
      { name: '양배추콘샐러드', calories: 95, isHealthy: true, image: require('../assets/images/main_icon/customer_icon.png') },
      { name: '피크닉제로', calories: 0, isHealthy: true, isDrink: true, image: require('../assets/images/main_icon/cupon_icon.png') },
    ],
    allergies: ['우유', '밀', '돼지고기'],
    totalCalories: 850,
  });

  // 즐겨찾기 토글
  const toggleFavorite = () => {
    setMenuData(prev => ({
      ...prev,
      mainDish: {
        ...prev.mainDish,
        isFavorite: !prev.mainDish.isFavorite
      }
    }));
  };

  // 총 칼로리 계산 (실제로는 API에서 받아올 수 있음)
  useEffect(() => {
    const total = menuData.mainDish.calories + 
                  menuData.sideDishes.reduce((sum, dish) => sum + dish.calories, 0);
    setMenuData(prev => ({ ...prev, totalCalories: total }));
  }, []);

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

  // 이미지 스와이프 아이템 (메인 + 함께하는 메뉴)
  const swipeItems = [
    { type: 'main', name: menuData.mainDish.name, image: menuData.mainDish.image, calories: menuData.mainDish.calories },
    ...menuData.sideDishes.map((d) => ({ type: 'side', name: d.name, image: d.image, calories: d.calories }))
  ];

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
          <TouchableOpacity style={styles.shareButton} activeOpacity={0.7}>
            <Ionicons name="share-outline" size={24} color="#666" />
          </TouchableOpacity>
          <TouchableOpacity style={styles.calendarButton} activeOpacity={0.7}>
            <Ionicons name="calendar-outline" size={24} color="#666" />
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView 
        style={styles.content} 
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        {/* 날짜 표시 */}
        <View style={styles.dateContainer}>
          <Ionicons name="time-outline" size={20} color="#1BB1E7" />
          <Text style={styles.dateText}>{menuData.date}</Text>
        </View>
        {/* 장소 표시 */}
        <View style={styles.dateContainer}>
          <MaterialIcons name="place" size={20} color="#1BB1E7" />
          <Text style={styles.dateText}>{menuData.place}</Text>
        </View>

        {/* 메인/사이드 이미지 스와이프 (풀폭) */}
        <View style={styles.imagePagerContainer}>
          <ScrollView
            horizontal
            pagingEnabled
            showsHorizontalScrollIndicator={false}
          >
            {swipeItems.map((item, index) => (
              <View key={index} style={styles.pagerItem}>
                <Image source={item.image} style={styles.pagerImage} />
                {isSoldOut && (
                  <View style={styles.soldOutOverlay}>
                    <Text style={styles.soldOutText}>품절</Text>
                  </View>
                )}
                <TouchableOpacity
                  style={styles.favoriteButton}
                  onPress={toggleFavorite}
                  activeOpacity={0.7}
                >
                  <Ionicons
                    name={menuData.mainDish.isFavorite ? 'heart' : 'heart-outline'}
                    size={24}
                    color={menuData.mainDish.isFavorite ? '#FF6B35' : '#999'}
                  />
                </TouchableOpacity>
              </View>
            ))}
          </ScrollView>
        </View>

        {/* 사이드 디시 리스트 */}
        <View style={styles.sideDishesSection}>
          <Text style={styles.sectionTitle}>함께하는 메뉴</Text>
          {menuData.sideDishes.map((dish, index) => (
            <View key={index} style={styles.sideDishItem}>
              <View style={styles.dishInfo}>
                <View style={styles.dishNameContainer}>
                  <Text style={styles.dishName}>{dish.name}</Text>
                  {dish.isHealthy && (
                    <View style={styles.healthyBadge}>
                      <Ionicons name="leaf-outline" size={12} color="#4CAF50" />
                      <Text style={styles.healthyText}>건강</Text>
                    </View>
                  )}
                  {dish.isDrink && (
                    <View style={styles.drinkBadge}>
                      <Ionicons name="water-outline" size={12} color="#2196F3" />
                      <Text style={styles.drinkText}>음료</Text>
                    </View>
                  )}
                </View>
                <Text style={styles.dishCalories}>{dish.calories} Kcal</Text>
              </View>
            </View>
          ))}
        </View>

        {/* 상세 영양 정보 */}
        <View style={styles.nutritionSection}>
          <Text style={styles.sectionTitle}>상세영양정보</Text>
          <View style={styles.nutritionCard}>
            <View style={styles.totalCaloriesContainer}>
              <Text style={styles.totalCaloriesLabel}>총 칼로리</Text>
              <Text style={styles.totalCaloriesValue}>{menuData.totalCalories} Kcal</Text>
            </View>
            
            <View style={styles.calorieProgressBar}>
              <View style={styles.progressBarBackground}>
                <View 
                  style={[
                    styles.progressBarFill, 
                    { width: `${Math.min((menuData.totalCalories / 2000) * 100, 100)}%` }
                  ]} 
                />
              </View>
              <Text style={styles.progressText}>일일 권장량의 {Math.round((menuData.totalCalories / 2000) * 100)}%</Text>
            </View>
          </View>
        </View>

        {/* 알레르기 정보 */}
        <View style={styles.allergySection}>
          <Text style={styles.sectionTitle}>알레르기 정보</Text>
          <View style={styles.allergyContainer}>
            {menuData.allergies.map((allergy, index) => (
              <View key={index} style={styles.allergyBadge}>
                <Ionicons name="warning-outline" size={16} color="#FF9800" />
                <Text style={styles.allergyText}>{allergy}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* 추가 기능 버튼들 */}
        <View style={styles.actionButtons}>
          <TouchableOpacity style={styles.actionButton} activeOpacity={0.7}>
            <Ionicons name="restaurant-outline" size={20} color="#666" />
            <Text style={styles.actionButtonText}>식당 위치</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.actionButton} onPress={handleCheckSoldOut} activeOpacity={0.7}>
            <Ionicons name={checkingSoldOut ? 'time-outline' : (isSoldOut ? 'close-circle-outline' : 'checkmark-circle-outline')} size={20} color={isSoldOut ? '#E53935' : '#4CAF50'} />
            <Text style={styles.actionButtonText}>{checkingSoldOut ? '확인 중...' : '품절 여부 확인'}</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

export default TodayMenuScreen;
