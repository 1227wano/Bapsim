import React, { useState } from 'react';
import { View, Text, SafeAreaView, StatusBar, TextInput, TouchableOpacity, Image, Alert, KeyboardAvoidingView, Platform } from 'react-native';
import { router } from 'expo-router';

const PRIMARY_COLOR = '#1BB1E7';

export default function SchoolSelectScreen() {
  const [schoolName, setSchoolName] = useState('');

  const handleNext = () => {
    const trimmed = schoolName.trim();
    if (!trimmed) {
      Alert.alert('학교 선택', '학교명을 입력해주세요.');
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
        <View style={{ flex: 0.7, paddingHorizontal: 24, justifyContent: 'center', alignItems: 'center' }}>
          {/* heyoung_logo 자리 (없으면 샘플 로고 사용) */}
          <Image
            source={require('../assets/images/heyoung_logo.jpg')}
            style={{ width: 200, height: 200, marginBottom: 28, resizeMode: 'contain' }}
          />

          <Text style={{ fontSize: 20, fontWeight: '700', color: '#222', marginBottom: 12 }}>학교 선택</Text>
          <TextInput
            placeholder="학교명을 입력하세요"
            placeholderTextColor="#aaa"
            value={schoolName}
            onChangeText={setSchoolName}
            style={{
              width: '100%',
              height: 48,
              borderWidth: 1,
              borderColor: '#e5e7eb',
              borderRadius: 10,
              paddingHorizontal: 14,
              marginBottom: 14,
              fontSize: 16,
              color: '#111827',
            }}
            returnKeyType="done"
            onSubmitEditing={handleNext}
          />

          <TouchableOpacity
            onPress={handleNext}
            activeOpacity={0.8}
            style={{
              width: '100%',
              height: 48,
              backgroundColor: PRIMARY_COLOR,
              borderRadius: 10,
              alignItems: 'center',
              justifyContent: 'center',
              marginTop: 16,
            }}
          >
            <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>다음</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}


