import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  SafeAreaView,
  Alert,
  Platform,
  StatusBar,
  Image
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './MyMenuScreen.styles';
import { router } from 'expo-router';

// 메뉴 버튼 컴포넌트
const MenuButton = ({ title, subtitle, icon, iconImage, hasNewBadge = false, isSpecial = false, onPress }) => {
  return (
    <TouchableOpacity 
      style={styles.menuButton} 
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={[styles.iconContainer, isSpecial && styles.specialIcon]}>
        {iconImage ? (
          <Image source={iconImage} style={styles.iconImage} resizeMode="contain" />
        ) : (
          <Text style={styles.iconText}>{icon}</Text>
        )}
        {hasNewBadge && (
          <View style={styles.newBadge}>
            <Text style={styles.newBadgeText}>NEW</Text>
          </View>
        )}
      </View>
      <Text style={styles.menuTitle} numberOfLines={2}>{title}</Text>
      {subtitle && <Text style={styles.menuSubtitle}>{subtitle}</Text>}
    </TouchableOpacity>
  );
};

// 메인 화면 컴포넌트
const MyMenuScreen = () => {

    const riceIconImage = require('../assets/images/main_icon/bmpm_icon.png') 
    const customerIconImage = require('../assets/images/main_icon/customer_icon.png') 
    const globalIconImage = require('../assets/images/main_icon/global_icon.png') 
    const homepageIconImage = require('../assets/images/main_icon/hompage_icon.png') 
    const jobIconImage = require('../assets/images/main_icon/job_icon.png') 
    const myplanIconImage = require('../assets/images/main_icon/myplan_icon.png') 
    const noticeIconImage = require('../assets/images/main_icon/notice_icon.png') 
    const schoolplanIconImage = require('../assets/images/main_icon/schoolplan_icon.png') 
    const shakeIconImage = require('../assets/images/main_icon/shake_icon.png') 
    const ttangIconImage = require('../assets/images/main_icon/ttang_icon.png') 
    const snslinkIconImage = require('../assets/images/main_icon/snslink_icon.png') 
    const cuponIconImage = require('../assets/images/main_icon/cupon_icon.png') 
  
  // 메뉴 클릭 핸들러
  const handleMenuPress = (menuName) => {
    if (menuName === '밥먹뽀먹') {
      // Expo Router를 사용해서 오늘의 메뉴 페이지로 이동
      router.push('/today-menu');
    } else if (menuName === '식권조회') {
      router.push('/tickets');
    }
  };

  const handleTabPress = (tabName) => {
    console.log(`${tabName} 탭 선택됨`);
    // 실제 개발에서는 네비게이션 로직 추가
  };

  const handleMorePress = () => {
    Alert.alert(
      '더보기', 
      '추가 옵션을 선택하세요',
      [
        { text: '설정', onPress: () => console.log('설정') },
        { text: '도움말', onPress: () => console.log('도움말') },
        { text: '취소', style: 'cancel' }
      ]
    );
  };

  const handleChatbotPress = () => {
    router.push('/chatbot');
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      
      <View style={styles.headerTopBar}>
        <Image
          source={require('../assets/images/main_icon/logo_sample.png')}
          style={styles.headerLogo}
          resizeMode="contain"
        />
        <TouchableOpacity onPress={handleChatbotPress} style={styles.chatIconButton} activeOpacity={0.7}>
          <Ionicons name="chatbubble-ellipses-outline" size={24} color="#666" />
        </TouchableOpacity>
      </View>

      <ScrollView 
        style={styles.content} 
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}
      >
        {/* 학생 정보 섹션 */}
        <View style={styles.studentSection}>
          <View style={styles.studentCard}>
            <Text style={styles.studentCardLabel}>모바일 학생증</Text>
            <View style={styles.studentTopRow}>
              <View style={styles.avatarCircle}>
                <Ionicons name="person" size={36} color="#1BB1E7" />
              </View>
              <View style={styles.studentTexts}>
                <Text style={styles.studentId}>1443254</Text>
                <Text style={styles.studentName}>김싸피</Text>
              </View>
            </View>
            <View style={styles.studentBadgeRow}>
              <TouchableOpacity style={styles.studentBadge} activeOpacity={0.7}>
                <Ionicons name="bluetooth" size={20} color="#fff" />
                <Text style={styles.studentBadgeText}>BT</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.studentBadge} activeOpacity={0.7}>
                <Ionicons name="qr-code-outline" size={20} color="#fff" />
                <Text style={styles.studentBadgeText}>QR</Text>
              </TouchableOpacity>
            </View>
          </View>
          <View style={styles.studentPoint}>
            <Text style={styles.pointName}>보유포인트</Text>
          </View>
        </View>
        <Text style={styles.headerTitle}>MY메뉴</Text>
        {/* 메뉴 섹션 */}
        <View style={styles.menuSection}>
          {/* 첫 번째 줄 메뉴 */}
          <View style={styles.menuRow}>
              <MenuButton
              title="밥먹뽀먹"
              iconImage={riceIconImage}
              hasNewBadge={true}
              isSpecial={true}
              onPress={() => handleMenuPress('밥먹뽀먹')}
            />
            <MenuButton
              title="식권조회"
              iconImage={cuponIconImage}
              onPress={() => handleMenuPress('식권조회')}
            />
            <MenuButton
              title="홈페이지"
              iconImage={homepageIconImage}
              onPress={() => handleMenuPress('홈페이지')}
            />
            <MenuButton
              title="학교공지사항"
              subtitle="학교"
              iconImage={noticeIconImage}
              onPress={() => handleMenuPress('학교공지사항')}
            />
          </View>

          {/* 두 번째 줄 메뉴 */}
          <View style={styles.menuRow}>
            <MenuButton
              title="학교SNS링크"
              subtitle="학교SNS"
              iconImage={snslinkIconImage}
              onPress={() => handleMenuPress('학교SNS링크')}
            />
            <MenuButton
              title="학사일정"
              iconImage={schoolplanIconImage}
              onPress={() => handleMenuPress('학사일정')}
            />
            <MenuButton
              title="나의일정관리"
              subtitle="나의일정"
              iconImage={myplanIconImage}
              onPress={() => handleMenuPress('나의일정관리')}
            />
            <MenuButton
              title="취업존"
              iconImage={jobIconImage}
              onPress={() => handleMenuPress('취업존')}
            />
          </View>

          {/* 세 번째 줄 메뉴 - 밥먹뽀먹 추가 */}
          <View style={styles.menuRow}>
            <MenuButton
              title="고객센터"
              iconImage={customerIconImage}
              onPress={() => handleMenuPress('고객센터')}
            />
            <MenuButton
              title="떙겨요"
              iconImage={ttangIconImage}
              onPress={() => handleMenuPress('떙겨요')}
            />
            <MenuButton
              title="언어설정"
              iconImage={globalIconImage}
              onPress={() => handleMenuPress('언어설정')}
            />
            <MenuButton
              title="흔들기"
              iconImage={shakeIconImage}
              onPress={() => handleMenuPress('흠뜨기')}
            />
          </View>
        </View>
      </ScrollView>

      {/* 하단 탭 바 */}
      <View style={styles.bottomTab}>
        <TouchableOpacity 
          style={styles.tabItem} 
          onPress={() => handleTabPress('학사')}
          activeOpacity={0.7}
        >
          <Ionicons name="school-outline" size={22} color="#999" />
          <Text style={styles.tabText}>학사</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.tabItem} 
          onPress={() => handleTabPress('혜택')}
          activeOpacity={0.7}
        >
          <View style={styles.newBadgeSmall}>
            <Text style={styles.newBadgeTextSmall}>NEW</Text>
          </View>
          <Ionicons name="gift-outline" size={22} color="#999" />
          <Text style={styles.tabText}>혜택</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.tabItem} 
          onPress={() => handleTabPress('전체 메뉴')}
          activeOpacity={0.7}
        >
          <Ionicons name="grid-outline" size={22} color="#999" />
          <Text style={styles.tabText}>전체 메뉴</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

export default MyMenuScreen;