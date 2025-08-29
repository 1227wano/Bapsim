import React, { useState } from 'react';
import { View, Text, SafeAreaView, StatusBar, TextInput, TouchableOpacity, Image, KeyboardAvoidingView, Platform } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient'; // 맨 위 import 추가

const PRIMARY_COLOR = '#1BB1E7';

export default function LoginScreen() {
  const params = useLocalSearchParams();
  const school = typeof params.school === 'string' ? params.school : undefined;

  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleLogin = () => {
    if (!userId.trim() || !password.trim()) {
      setErrorMessage('아이디와 비밀번호를 입력해주세요.');
      return;
    }
  
    // TODO: 실제 API 연동
    const loginSuccess = fakeLoginApi(userId, password);
  
    if (loginSuccess) {
      setErrorMessage('');
      router.replace('/(tabs)');
    } else {
      setErrorMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  const fakeLoginApi = (id: string, pw: string) => {
    return id === 'test' && pw === '1234'; // 맞으면 true
  };

  const handleBackToSchool = () => {
    router.back();
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#fff' }}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <View style={{ flex: 1, justifyContent: 'center' }}>
          {/* 학교 로고 */}
          <LinearGradient
            colors={[PRIMARY_COLOR, '#fff']} // 위는 연보라, 아래로 점점 흰색
            style={{ flex: 0.4, alignItems: 'center', justifyContent: 'center' }}
          >
            <Image
              source={require('../assets/images/main_icon/logo_sample.png')}
              style={{
                width: 220,
                height: 220,
                marginBottom: 28,
                resizeMode: 'contain',
                shadowColor: '#000',
                shadowOffset: { width: 0, height: 6 },
                shadowOpacity: 0.15,
                shadowRadius: 10,
                elevation: 8,
              }}
            />
          </LinearGradient>

          {/* 아이디, 비밀번호 */}
          <View style={{ flex: 0.7, paddingHorizontal: 24, alignItems: 'center', gap: 12 }}>
            {school && (
              <Text style={{ marginTop: 12, color: '#111827', fontSize: 20, fontWeight: 700 }}>{school}</Text>
            )}
            <TextInput
              placeholder="학번"
              value={userId}
              onChangeText={setUserId}
              style={{
                width: '100%',
                height: 50,
                borderWidth: 1,
                borderColor: '#d1d5db',
                borderRadius: 12,
                paddingHorizontal: 16,
                marginBottom: 10,
                fontSize: 16,
                color: '#111827',
                backgroundColor: '#fff',
                shadowColor: '#000',
                shadowOffset: { width: 0, height: 3 },
                shadowOpacity: 0.1,
                shadowRadius: 5,
                elevation: 3,
              }}
              returnKeyType="done"
              onSubmitEditing={handleLogin}
            />
            <TextInput
              placeholder="비밀번호"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              style={{
                width: '100%',
                height: 50,
                borderWidth: 1,
                borderColor: '#d1d5db',
                borderRadius: 12,
                paddingHorizontal: 16,
                fontSize: 16,
                color: '#111827',
                backgroundColor: '#fff',
                shadowColor: '#000',
                shadowOffset: { width: 0, height: 3 },
                shadowOpacity: 0.1,
                shadowRadius: 5,
                elevation: 3,
              }}
              onSubmitEditing={handleLogin}
            />
            {errorMessage ? (
              <Text style={{ color: 'red', fontSize: 14, marginTop: 4 }}>{errorMessage}</Text>
            ) : null}

            {/* 로그인 버튼 */}
            <TouchableOpacity
              onPress={handleLogin}
              activeOpacity={0.8}
              style={{
                width: '100%',
                height: 50,
                backgroundColor: PRIMARY_COLOR,
                borderRadius: 12,
                alignItems: 'center',
                justifyContent: 'center',
                marginTop: 10,
                shadowColor: '#000',
                shadowOffset: { width: 0, height: 4 },
                shadowOpacity: 0.2,
                shadowRadius: 6,
                elevation: 4,
              }}
              >
              <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>로그인</Text>
            </TouchableOpacity>
          {/* 다른 학교 선택 */}
          <TouchableOpacity onPress={handleBackToSchool} style={{ alignSelf: 'center', marginTop: 14 }}>
            <Text style={{ color: PRIMARY_COLOR, fontSize: 14 }}>다른 학교 선택</Text>
          </TouchableOpacity>
          </View>

        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}


