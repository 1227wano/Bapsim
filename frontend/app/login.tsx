import React, { useState } from 'react';
import { View, Text, SafeAreaView, StatusBar, TextInput, TouchableOpacity, Image, KeyboardAvoidingView, Platform, Alert } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient'; // 맨 위 import 추가
import { Config } from '../constants/Config';

const PRIMARY_COLOR = '#1BB1E7';

export default function LoginScreen() {
  const params = useLocalSearchParams();
  const school = typeof params.school === 'string' ? params.school : undefined;

  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const performLogin = async (id: string, pw: string) => {
    try {
      const response = await fetch('http://localhost:8082/api/members/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: id,
          userPass: pw,
        }),
      });

      if (response.ok) {
        const loginData = await response.json();
        return { success: true, data: loginData };
      } else {
        return { success: false, status: response.status };
      }
    } catch (error) {
      console.error('로그인 중 오류:', error);
      return { success: false, error: error };
    }
  };

  const handleLogin = async () => {
    if (!userId.trim() || !password.trim()) {
      setErrorMessage('아이디와 비밀번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setErrorMessage('');

    try {
      // 로그인 시도
      const loginResult = await performLogin(userId, password);
      
      if (loginResult.success) {
        setErrorMessage('');
        // 로그인 성공 시 메인 화면으로 이동
        router.replace('/(tabs)');
      } else {
        if (loginResult.status === 401) {
          setErrorMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
        } else {
          setErrorMessage('로그인 중 오류가 발생했습니다. 다시 시도해주세요.');
        }
      }
    } catch (error) {
      setErrorMessage('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
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
              }}
            />
          </LinearGradient>

          {/* 아이디, 비밀번호 */}
          <View style={{ flex: 0.7, paddingHorizontal: 24, alignItems: 'center', gap: 12 }}>
            {school && (
              <Text style={{ marginTop: 12, color: '#111827', fontSize: 20, fontWeight: '700' }}>{school}</Text>
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
              editable={!isLoading}
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
              editable={!isLoading}
            />
            {errorMessage ? (
              <Text style={{ color: 'red', fontSize: 14, marginTop: 4 }}>{errorMessage}</Text>
            ) : null}

            {/* 로그인 버튼 */}
            <TouchableOpacity
              onPress={handleLogin}
              activeOpacity={0.8}
              disabled={isLoading}
              style={{
                width: '100%',
                height: 50,
                backgroundColor: isLoading ? '#ccc' : PRIMARY_COLOR,
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
              <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>
                {isLoading ? '로그인 중...' : '로그인'}
              </Text>
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


