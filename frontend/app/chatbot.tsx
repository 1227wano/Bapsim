import React, { useState, useRef, useEffect } from 'react';
import { View, Text, SafeAreaView, StatusBar, TouchableOpacity, TextInput, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { styles } from '../screens/ChatbotScreen.styles';

type ChatMessage = {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  createdAt: number;
};

const ChatbotScreen = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      role: 'assistant',
      content: '안녕하세요! 무엇을 도와드릴까요? 챗봇은 현재 베타이며, 곧 API와 연동됩니다.',
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

    // TODO: 실제 API 연동 예정. 아래는 임시 응답 시뮬레이션입니다.
    try {
      await new Promise(res => setTimeout(res, 700));
      const assistantMsg: ChatMessage = {
        id: `a-${Date.now()}`,
        role: 'assistant',
        content: '현재는 시범 서비스입니다. 곧 실제 챗봇과 연결됩니다! \n요청: ' + userMsg.content,
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
        <Text style={styles.headerTitle}>챗봇 (베타)</Text>
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
              <Text style={[styles.messageText, m.role === 'user' ? styles.userText : styles.assistantText]}>
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


