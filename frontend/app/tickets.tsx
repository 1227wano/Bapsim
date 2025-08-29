import React, { useState } from 'react';
import { View, Text, SafeAreaView, StatusBar, ScrollView, TouchableOpacity, Image, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

const PRIMARY_COLOR = '#1BB1E7';

interface Ticket {
  id: string;
  menuName: string;
  restaurant: string;
  date: string;
  mealType: string;
  price: number;
  isUsed: boolean;
  usedAt?: string;
}

export default function TicketsScreen() {
  const [tickets] = useState<Ticket[]>([
    {
      id: 'T001',
      menuName: '돈까스 정식',
      restaurant: '학생식당 1층',
      date: '2024-01-15',
      mealType: '점심',
      price: 4500,
      isUsed: false,
    },
    {
      id: 'T002',
      menuName: '김치찌개',
      restaurant: '교직원식당',
      date: '2024-01-15',
      mealType: '점심',
      price: 4000,
      isUsed: true,
      usedAt: '2024-01-15 12:30',
    },
    {
      id: 'T003',
      menuName: '치킨가라아게',
      restaurant: '학생식당 2층',
      date: '2024-01-14',
      mealType: '저녁',
      price: 5000,
      isUsed: true,
      usedAt: '2024-01-14 18:15',
    },
    {
      id: 'T004',
      menuName: '비빔밥',
      restaurant: '학생식당 1층',
      date: '2024-01-14',
      mealType: '점심',
      price: 4200,
      isUsed: false,
    },
    {
      id: 'T005',
      menuName: '라면',
      restaurant: '편의점',
      date: '2024-01-13',
      mealType: '저녁',
      price: 3500,
      isUsed: true,
      usedAt: '2024-01-13 19:45',
    },
  ]);

  const handleBack = () => {
    router.back();
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString();
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={handleBack} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>식권 조회</Text>
        <View style={styles.placeholder} />
      </View>

      {/* 식권 목록 */}
      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.ticketList}>
          {tickets.map((ticket) => (
            <View key={ticket.id} style={[styles.ticketCard, ticket.isUsed && styles.usedTicket]}>
              {/* 식권 정보 */}
              <View style={styles.ticketInfo}>
                <View style={styles.ticketHeader}>
                  <Text style={styles.menuName}>{ticket.menuName}</Text>
                  {ticket.isUsed && (
                    <View style={styles.usedBadge}>
                      <Text style={styles.usedBadgeText}>사용완료</Text>
                    </View>
                  )}
                </View>
                
                <View style={styles.ticketDetails}>
                  <View style={styles.detailRow}>
                    <Ionicons name="location" size={16} color="#666" />
                    <Text style={styles.detailText}>{ticket.restaurant}</Text>
                  </View>
                  
                  <View style={styles.detailRow}>
                    <Ionicons name="calendar" size={16} color="#666" />
                    <Text style={styles.detailText}>
                      {formatDate(ticket.date)} {ticket.mealType}
                    </Text>
                  </View>
                  
                  <View style={styles.detailRow}>
                    <Ionicons name="card" size={16} color="#666" />
                    <Text style={styles.detailText}>{formatPrice(ticket.price)}원</Text>
                  </View>
                  
                  {ticket.isUsed && ticket.usedAt && (
                    <View style={styles.detailRow}>
                      <Ionicons name="time" size={16} color="#666" />
                      <Text style={styles.detailText}>사용: {ticket.usedAt}</Text>
                    </View>
                  )}
                </View>
              </View>

              {/* QR 코드 */}
              <View style={styles.qrSection}>
                <Image
                  source={require('../assets/images/QR_code.svg')}
                  style={styles.qrCode}
                  resizeMode="contain"
                />
                <Text style={styles.ticketId}>{ticket.id}</Text>
              </View>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e9ecef',
  },
  backButton: {
    padding: 8,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
  },
  placeholder: {
    width: 40,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 20,
  },
  ticketList: {
    gap: 16,
  },
  ticketCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
    flexDirection: 'row',
    alignItems: 'center',
  },
  usedTicket: {
    opacity: 0.7,
    backgroundColor: '#f8f9fa',
  },
  ticketInfo: {
    flex: 1,
    marginRight: 20,
  },
  ticketHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  menuName: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    flex: 1,
  },
  usedBadge: {
    backgroundColor: '#6c757d',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    marginLeft: 8,
  },
  usedBadgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  ticketDetails: {
    gap: 8,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  detailText: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  qrSection: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  qrCode: {
    width: 80,
    height: 80,
    marginBottom: 8,
  },
  ticketId: {
    fontSize: 12,
    color: '#999',
    fontWeight: '500',
  },
});
