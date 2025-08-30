import React, { useState, useEffect } from 'react';
import { View, Text, SafeAreaView, StatusBar, TouchableOpacity, Image, Alert, KeyboardAvoidingView, Platform, Modal } from 'react-native';
import { router } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient'; // 맨 위 import 추가
import { Ionicons } from '@expo/vector-icons';

const PRIMARY_COLOR = '#1BB1E7';
const BACKGROUND_COLOR = '#E4E5FF'; // 이미지 참고 연보라 배경

interface University {
  uniId: number;
  uniName: string;
}

export default function SchoolSelectScreen() {
  const [schoolName, setSchoolName] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [universities, setUniversities] = useState<University[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchUniversities();
  }, []);

  const fetchUniversities = async () => {
    // API 호출 없이 바로 기본 데이터 세팅
    setUniversities([
      { uniId: 1, uniName: '싸피대학교' },
    ]);
    setIsLoading(false);
  };

  const handleSchoolSelect = (school: string) => {
    setSchoolName(school);
    setIsDropdownOpen(false);
  };

  const handleNext = () => {
    const trimmed = schoolName.trim();
    if (!trimmed) {
      Alert.alert('학교 선택', '학교를 선택해주세요.');
      return;
    }
    if (Platform.OS === 'web') {
      const ok = typeof window !== 'undefined' ? window.confirm(`선택하신 ${trimmed}로 인증하시겠어요?`) : true;
      if (ok) {
        router.push({ pathname: '/login', params: { school: trimmed } });
      }
      return;
    }
    Alert.alert(
      '학교 인증',
      `선택하신 ${trimmed}로 인증하시겠어요?`,
      [
        { text: '취소', style: 'cancel' },
        {
          text: '확인',
          onPress: () => router.push({ pathname: '/login', params: { school: trimmed } }),
        },
      ]
    );
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#fff' }}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>

        {/* 상단 배경 영역 (그라데이션 적용) */}
        <LinearGradient
          colors={[BACKGROUND_COLOR, '#fff']} // 위는 연보라, 아래로 점점 흰색
          style={{ flex: 0.38, justifyContent: 'center', alignItems: 'center' }}
        >
          <Image
            source={require('../assets/images/heyoung_logo.jpg')}
            style={{
              width: 220,
              height: 220,
              marginBottom: 28,
              resizeMode: 'contain',
              shadowColor: '#000',
              shadowOffset: { width: 0, height: 6 },
              shadowOpacity: 0.15,
              shadowRadius: 10,
            }}
          />
        </LinearGradient>

        {/* 입력창 + 버튼 영역 */}
        <View style={{ flex: 0.5, paddingHorizontal: 24, alignItems: 'center' }}>
          <Text style={{ fontSize: 24, fontWeight: '700', color: '#333', marginBottom: 12, marginTop: 8 }}>학교 선택</Text>
          
          {/* 학교 선택 드롭다운 */}
          <TouchableOpacity
            onPress={() => setIsDropdownOpen(true)}
            style={{
              width: '100%',
              height: 50,
              borderWidth: 1,
              borderColor: '#d1d5db',
              borderRadius: 12,
              paddingHorizontal: 16,
              marginBottom: 16,
              backgroundColor: '#fff',
              shadowColor: '#000',
              shadowOffset: { width: 0, height: 3 },
              shadowOpacity: 0.1,
              shadowRadius: 5,
              elevation: 3,
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <Text style={{ fontSize: 16, color: schoolName ? '#111827' : '#999' }}>
              {schoolName || '학교 선택'}
            </Text>
            <Ionicons name="search" size={20} color="#666" />
          </TouchableOpacity>

          <TouchableOpacity
            onPress={handleNext}
            activeOpacity={0.8}
            style={{
              width: '100%',
              height: 50,
              backgroundColor: PRIMARY_COLOR,
              borderRadius: 12,
              alignItems: 'center',
              justifyContent: 'center',
              shadowColor: '#000',
              shadowOffset: { width: 0, height: 4 },
              shadowOpacity: 0.2,
              shadowRadius: 6,
              elevation: 4,
            }}
          >
            <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>다음</Text>
          </TouchableOpacity>
        </View>

        {/* 드롭다운 모달 */}
        <Modal
          visible={isDropdownOpen}
          transparent={true}
          animationType="fade"
          onRequestClose={() => setIsDropdownOpen(false)}
        >
          <TouchableOpacity
            style={{
              flex: 1,
              backgroundColor: 'rgba(0, 0, 0, 0.5)',
              justifyContent: 'center',
              alignItems: 'center',
            }}
            activeOpacity={1}
            onPress={() => setIsDropdownOpen(false)}
          >
            <View
              style={{
                backgroundColor: '#fff',
                borderRadius: 12,
                padding: 20,
                width: '80%',
                maxHeight: '60%',
              }}
            >
              <Text style={{ fontSize: 18, fontWeight: '700', color: '#333', marginBottom: 16, textAlign: 'center' }}>
                학교 선택
              </Text>
              {isLoading ? (
                <Text style={{ textAlign: 'center', color: '#666', padding: 20 }}>학교 목록을 불러오는 중...</Text>
              ) : (
                universities.map((university) => (
                  <TouchableOpacity
                    key={university.uniId}
                    onPress={() => handleSchoolSelect(university.uniName)}
                    style={{
                      paddingVertical: 12,
                      paddingHorizontal: 16,
                      borderBottomWidth: 1,
                      borderBottomColor: '#f0f0f0',
                    }}
                  >
                    <Text style={{ fontSize: 16, color: '#333' }}>{university.uniName}</Text>
                  </TouchableOpacity>
                ))
              )}
            </View>
          </TouchableOpacity>
        </Modal>

      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
