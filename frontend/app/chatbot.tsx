import React, { useState, useRef, useEffect } from 'react';
import { View, Text, SafeAreaView, StatusBar, TouchableOpacity, TextInput, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { router, useLocalSearchParams } from 'expo-router';
import { styles } from '../screens/ChatbotScreen.styles';

type ChatMessage = {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  createdAt: number;
};

const ChatbotScreen = () => {
  const API_URL = 'http://3.39.192.187:8000/chat';
  const params = useLocalSearchParams();
  const currentUserId = typeof params.user_id === 'string' ? params.user_id : 'u1';
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      role: 'assistant',
      content: '안녕하세요! 당신의 학식메이트 뽀먹이에요! 무엇을 도와드릴까요?',
      createdAt: Date.now(),
    },
  ]);
  const [input, setInput] = useState('');
  const [isSending, setIsSending] = useState(false);
  const scrollRef = useRef<ScrollView>(null);

  useEffect(() => {
    scrollRef.current?.scrollToEnd({ animated: true });
  }, [messages]);

  const handleBack = () => router.back();

  const sendMessage = async () => {
    if (!input.trim() || isSending) return;
    const userMsg: ChatMessage = {
      id: `u-${Date.now()}`,
      role: 'user',
      content: input.trim(),
      createdAt: Date.now(),
    };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsSending(true);

    try {
        // 🔥 이전 대화 내역 + 새 메시지까지 포함한 context 생성
      const fullContext = [...messages, userMsg].map(msg => ({
        role: msg.role,
        content: msg.content,
      }));

      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: currentUserId,
          message: userMsg.content,
          context: { locale: 'ko' },
          history: fullContext,
        }),
      });
      let replyText = '';
      if (response.ok) {
        const data = await response.json();
        replyText = typeof data?.reply === 'string' ? data.reply : '응답을 이해하지 못했습니다.';
      } else {
        replyText = `서버 오류가 발생했습니다. (HTTP ${response.status})`;
      }
      const assistantMsg: ChatMessage = {
        id: `a-${Date.now()}`,
        role: 'assistant',
        content: replyText,
        createdAt: Date.now(),
      };
      setMessages(prev => [...prev, assistantMsg]);
    } catch (e: any) {
      const assistantMsg: ChatMessage = {
        id: `a-${Date.now()}`,
        role: 'assistant',
        content: '네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
        createdAt: Date.now(),
      };
      setMessages(prev => [...prev, assistantMsg]);
    } finally {
      setIsSending(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />

      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={handleBack} style={styles.backButton} activeOpacity={0.7}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>챗봇</Text>
        <View style={{ width: 24 }} />
      </View>

      <KeyboardAvoidingKeyboardWrapper>
        <ScrollView
          ref={scrollRef}
          style={styles.messages}
          contentContainerStyle={styles.messagesContent}
          showsVerticalScrollIndicator={false}
        >
          {messages.map(m => (
            <View key={m.id} style={[styles.messageBubble, m.role === 'user' ? styles.userBubble : styles.assistantBubble]}>
              <Text style={m.role === 'user' ? styles.userText : styles.assistantText}>
                {m.content}
              </Text>
            </View>
          ))}
          {isSending && (
            <View style={[styles.messageBubble, styles.assistantBubble]}>
              <Text style={styles.assistantText}>입력하신 내용을 분석 중입니다…</Text>
            </View>
          )}
        </ScrollView>

        {/* 입력 박스 */}
        <View style={styles.inputBar}>
          <TextInput
            style={styles.input}
            value={input}
            onChangeText={setInput}
            placeholder="메시지를 입력하세요"
            placeholderTextColor="#aaa"
            onSubmitEditing={sendMessage}
            returnKeyType="send"
          />
          <TouchableOpacity onPress={sendMessage} style={styles.sendButton} disabled={isSending} activeOpacity={0.7}>
            <Ionicons name="send" size={18} color="#fff" />
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingKeyboardWrapper>
    </SafeAreaView>
  );
};

const KeyboardAvoidingKeyboardWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  if (Platform.OS === 'ios') {
    return (
      <KeyboardAvoidingView behavior="padding" style={{ flex: 1 }}>
        {children}
      </KeyboardAvoidingView>
    );
  }
  return <View style={{ flex: 1 }}>{children}</View>;
};

export default ChatbotScreen;


