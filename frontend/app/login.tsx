import React, { useState } from 'react';
import { View, Text, SafeAreaView, StatusBar, TextInput, TouchableOpacity, Image, KeyboardAvoidingView, Platform } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';

const PRIMARY_COLOR = '#1BB1E7';

export default function LoginScreen() {
  const params = useLocalSearchParams();
  const school = typeof params.school === 'string' ? params.school : undefined;

  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    // TODO: 실제 로그인 API 연동
    if (!userId.trim() || !password.trim()) return;
    router.replace('/(tabs)');
  };

  const handleBackToSchool = () => {
    router.back();
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#fff' }}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <View style={{ flex: 0.7, paddingHorizontal: 24, justifyContent: 'center' }}>
          {/* 학교 로고 */}
          <View style={{ alignItems: 'center', marginBottom: 24 }}>
            <Image
              source={require('../assets/images/main_icon/logo_sample.png')}
              style={{ width: 200, height: 200, resizeMode: 'contain' }}
            />
            {school && (
              <Text style={{ marginTop: 12, color: '#111827', fontSize: 20, fontWeight: 700 }}>{school}</Text>
            )}
          </View>

          {/* 아이디, 비밀번호 */}
          <View style={{ gap: 12 }}>
            <TextInput
              placeholder="학번"
              placeholderTextColor="#aaa"
              value={userId}
              onChangeText={setUserId}
              autoCapitalize="none"
              style={{
                height: 48,
                borderWidth: 1,
                borderColor: '#e5e7eb',
                borderRadius: 10,
                paddingHorizontal: 14,
                fontSize: 16,
                color: '#111827',
              }}
              returnKeyType="next"
            />
            <TextInput
              placeholder="비밀번호"
              placeholderTextColor="#aaa"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              style={{
                height: 48,
                borderWidth: 1,
                borderColor: '#e5e7eb',
                borderRadius: 10,
                paddingHorizontal: 14,
                fontSize: 16,
                color: '#111827',
              }}
              returnKeyType="go"
              onSubmitEditing={handleLogin}
            />
          </View>

          {/* 로그인 버튼 */}
          <TouchableOpacity
            onPress={handleLogin}
            activeOpacity={0.8}
            style={{
              height: 48,
              backgroundColor: PRIMARY_COLOR,
              borderRadius: 10,
              alignItems: 'center',
              justifyContent: 'center',
              marginTop: 16,
            }}
          >
            <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>로그인</Text>
          </TouchableOpacity>

          {/* 다른 학교 선택 */}
          <TouchableOpacity onPress={handleBackToSchool} style={{ alignSelf: 'center', marginTop: 14 }}>
            <Text style={{ color: PRIMARY_COLOR, fontSize: 14 }}>다른 학교 선택</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}


