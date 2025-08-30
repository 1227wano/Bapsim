import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, SafeAreaView, StatusBar, ScrollView, TouchableOpacity, Image, TextInput, Alert, Modal, StyleSheet, Platform } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Config } from '../constants/Config';

const PRIMARY_COLOR = '#1BB1E7';

interface PaymentPinModalProps {
  visible: boolean;
  onClose: () => void;
  onPinSubmit: (pin: string) => void;
  isRegistration?: boolean;
  errorMessage?: string;
  setErrorMessage?: (message: string) => void;
}

interface QRCodeModalProps {
  visible: boolean;
  onClose: () => void;
  isoDate: string;
  mainName: string;
  mealType: string;
}

// QR코드 모달 컴포넌트
const QRCodeModal: React.FC<QRCodeModalProps> = ({ 
  visible, 
  onClose, 
  isoDate, 
  mainName,
  mealType
}) => {
  if (!visible) return null;

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="fade"
      onRequestClose={onClose}
      statusBarTranslucent={false}
    >
      <View style={styles.qrModalOverlay}>
        <View style={styles.qrModalContent}>
          <View style={styles.qrModalHeader}>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Ionicons name="close" size={24} color="#333" />
            </TouchableOpacity>
            <Text style={styles.qrModalTitle}>결제 완료</Text>
            <View style={styles.placeholder} />
          </View>

          <View style={styles.qrContent}>
            <Text style={styles.qrSubtitle}>주문이 성공적으로 완료되었습니다!</Text>
            
            {/* QR코드 이미지 */}
            <View style={styles.qrCodeContainer}>
              <Image
                source={require('../assets/images/QR_code.jpg')}
                style={styles.qrCodeImage}
                resizeMode="contain"
              />
            </View>

                         {/* 결제 정보 */}
             <View style={styles.paymentInfo}>
               <View style={styles.infoRow}>
                 <Text style={styles.infoLabel}>결제 날짜:</Text>
                 <Text style={styles.infoValue}>{isoDate}</Text>
               </View>
               <View style={styles.infoRow}>
                 <Text style={styles.infoLabel}>주문 메뉴:</Text>
                 <Text style={styles.infoValue}>{mealType}</Text>
               </View>
             </View>

              {/* 확인 버튼 */}
             <TouchableOpacity
               style={styles.confirmButton}
               onPress={() => {
                 onClose();
                 router.push('/(tabs)');
               }}
             >
               <Text style={styles.confirmButtonText}>확인</Text>
             </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

// 웹 전용 PIN 모달 컴포넌트
const WebPaymentPinModal: React.FC<PaymentPinModalProps> = ({ 
  visible, 
  onClose, 
  onPinSubmit, 
  isRegistration = false,
  errorMessage = '',
  setErrorMessage
}) => {
  const [pin, setPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [isConfirmMode, setIsConfirmMode] = useState(false);

  const handlePinSubmit = () => {
    if (setErrorMessage) setErrorMessage(''); // 오류 메시지 초기화
    
    if (isRegistration) {
      if (pin.length !== 4) {
        if (setErrorMessage) setErrorMessage('PIN 번호는 4자리여야 합니다.');
        return;
      }
      if (!isConfirmMode) {
        setIsConfirmMode(true);
        setConfirmPin('');
        return;
      }
      if (pin !== confirmPin) {
        if (setErrorMessage) setErrorMessage('PIN 번호가 일치하지 않습니다.');
        setConfirmPin('');
        return;
      }
    } else {
      if (pin.length !== 4) {
        if (setErrorMessage) setErrorMessage('PIN 번호는 4자리여야 합니다.');
        return;
      }
    }
    
    onPinSubmit(pin);
    handleClose();
  };

  const handleClose = () => {
    setPin('');
    setConfirmPin('');
    setIsConfirmMode(false);
    if (setErrorMessage) setErrorMessage(''); // 오류 메시지 초기화
    onClose();
  };

  const handleNumberPress = (num: string) => {
    if (setErrorMessage) setErrorMessage(''); // 숫자 입력 시 오류 메시지 초기화
    
    if (isRegistration && isConfirmMode) {
      if (confirmPin.length < 4) {
        setConfirmPin(prev => prev + num);
      }
    } else {
      if (pin.length < 4) {
        setPin(prev => prev + num);
      }
    }
  };

  const handleDelete = () => {
    if (isRegistration && isConfirmMode) {
      setConfirmPin(prev => prev.slice(0, -1));
    } else {
      setPin(prev => prev.slice(0, -1));
    }
  };

  const currentPin = isRegistration && isConfirmMode ? confirmPin : pin;
  const currentPinLength = currentPin.length;

  if (!visible) return null;

  return (
    <View style={styles.webModalOverlay}>
      <View style={styles.webModalContent}>
        <View style={styles.modalHeader}>
          <TouchableOpacity onPress={handleClose} style={styles.closeButton}>
            <Ionicons name="close" size={24} color="#333" />
          </TouchableOpacity>
          <Text style={styles.modalTitle}>
            {isRegistration ? 'PIN 번호 등록' : 'PIN 번호 입력'}
          </Text>
          <View style={styles.placeholder} />
        </View>

        <View style={styles.pinSection}>
          <Text style={styles.pinLabel}>
            {isRegistration 
              ? (isConfirmMode ? 'PIN 번호 확인' : 'PIN 번호 입력')
              : 'PIN 번호를 입력해주세요'
            }
          </Text>
          
          {/* PIN 번호 표시 */}
          <View style={styles.pinDisplay}>
            {[0, 1, 2, 3].map((index) => (
              <View key={index} style={[
                styles.pinDot,
                index < currentPinLength && styles.pinDotFilled
              ]} />
            ))}
          </View>

          {/* 오류 메시지 */}
          {errorMessage ? (
            <Text style={styles.errorMessage}>{errorMessage}</Text>
          ) : null}

          {/* 숫자 키패드 */}
          <View style={styles.keypad}>
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
              <TouchableOpacity
                key={num}
                style={styles.keypadButton}
                onPress={() => handleNumberPress(String(num))}
              >
                <Text style={styles.keypadText}>{num}</Text>
              </TouchableOpacity>
            ))}
            <TouchableOpacity
              style={styles.keypadButton}
              onPress={() => handleNumberPress('0')}
            >
              <Text style={styles.keypadText}>0</Text>
            </TouchableOpacity>
            <View style={styles.keypadEmpty} />
            <TouchableOpacity style={styles.keypadButton} onPress={handleDelete}>
              <Text style={styles.keypadText}>←</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            style={[
              styles.submitButton,
              currentPinLength === 4 && styles.submitButtonActive
            ]}
            onPress={handlePinSubmit}
            disabled={currentPinLength !== 4}
          >
            <Text style={[
              styles.submitButtonText,
              currentPinLength === 4 && styles.submitButtonTextActive
            ]}>
              {isRegistration 
                ? (isConfirmMode ? '확인' : '다음')
                : '확인'
              }
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
};

// 네이티브 전용 PIN 모달 컴포넌트
const NativePaymentPinModal: React.FC<PaymentPinModalProps> = ({ 
  visible, 
  onClose, 
  onPinSubmit, 
  isRegistration = false,
  errorMessage = '',
  setErrorMessage
}) => {
  const [pin, setPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [isConfirmMode, setIsConfirmMode] = useState(false);

  const handlePinSubmit = () => {
    if (setErrorMessage) setErrorMessage(''); // 오류 메시지 초기화
    
    if (isRegistration) {
      if (pin.length !== 4) {
        if (setErrorMessage) setErrorMessage('PIN 번호는 4자리여야 합니다.');
        return;
      }
      if (!isConfirmMode) {
        setIsConfirmMode(true);
        setConfirmPin('');
        return;
      }
      if (pin !== confirmPin) {
        if (setErrorMessage) setErrorMessage('PIN 번호가 일치하지 않습니다.');
        setConfirmPin('');
        return;
      }
    } else {
      if (pin.length !== 4) {
        if (setErrorMessage) setErrorMessage('PIN 번호는 4자리여야 합니다.');
        return;
      }
    }
    
    onPinSubmit(pin);
    handleClose();
  };

  const handleClose = () => {
    setPin('');
    setConfirmPin('');
    setIsConfirmMode(false);
    if (setErrorMessage) setErrorMessage(''); // 오류 메시지 초기화
    onClose();
  };

  const handleNumberPress = (num: string) => {
    if (setErrorMessage) setErrorMessage(''); // 숫자 입력 시 오류 메시지 초기화
    
    if (isRegistration && isConfirmMode) {
      if (confirmPin.length < 4) {
        setConfirmPin(prev => prev + num);
      }
    } else {
      if (pin.length < 4) {
        setPin(prev => prev + num);
      }
    }
  };

  const handleDelete = () => {
    if (isRegistration && isConfirmMode) {
      setConfirmPin(prev => prev.slice(0, -1));
    } else {
      setPin(prev => prev.slice(0, -1));
    }
  };

  const currentPin = isRegistration && isConfirmMode ? confirmPin : pin;
  const currentPinLength = currentPin.length;

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="slide"
      onRequestClose={handleClose}
      statusBarTranslucent={false}
    >
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={handleClose} style={styles.closeButton}>
              <Ionicons name="close" size={24} color="#333" />
            </TouchableOpacity>
            <Text style={styles.modalTitle}>
              {isRegistration ? 'PIN 번호 등록' : 'PIN 번호 입력'}
            </Text>
            <View style={styles.placeholder} />
          </View>

          <View style={styles.pinSection}>
            <Text style={styles.pinLabel}>
              {isRegistration 
                ? (isConfirmMode ? 'PIN 번호 확인' : 'PIN 번호 입력')
                : 'PIN 번호를 입력해주세요'
              }
            </Text>
            
            {/* PIN 번호 표시 */}
            <View style={styles.pinDisplay}>
              {[0, 1, 2, 3].map((index) => (
                <View key={index} style={[
                  styles.pinDot,
                  index < currentPinLength && styles.pinDotFilled
                ]} />
              ))}
            </View>

            {/* 오류 메시지 */}
            {errorMessage ? (
              <Text style={styles.errorMessage}>{errorMessage}</Text>
            ) : null}

            {/* 숫자 키패드 */}
          <View style={styles.keypad}>
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
              <TouchableOpacity
                key={num}
                style={styles.keypadButton}
                onPress={() => handleNumberPress(String(num))}
              >
                <Text style={styles.keypadText}>{num}</Text>
              </TouchableOpacity>
            ))}
            <TouchableOpacity
              style={styles.keypadButton}
              onPress={() => handleNumberPress('0')}
            >
              <Text style={styles.keypadText}>0</Text>
            </TouchableOpacity>
            <View style={styles.keypadEmpty} />
            <TouchableOpacity style={styles.keypadButton} onPress={handleDelete}>
              <Text style={styles.keypadText}>←</Text>
            </TouchableOpacity>
          </View>

            <TouchableOpacity
              style={[
                styles.submitButton,
                currentPinLength === 4 && styles.submitButtonActive
              ]}
              onPress={handlePinSubmit}
              disabled={currentPinLength !== 4}
            >
              <Text style={[
                styles.submitButtonText,
                currentPinLength === 4 && styles.submitButtonTextActive
              ]}>
                {isRegistration 
                  ? (isConfirmMode ? '확인' : '다음')
                  : '확인'
                }
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

export default function PaymentScreen() {
  const params = useLocalSearchParams();
  // 파라미터에서 데이터 추출
  const title = typeof params.title === 'string' ? params.title : '';
  const mainName = typeof params.mainName === 'string' ? params.mainName : '';
  const isoDate = typeof params.isoDate === 'string' ? params.isoDate : '';
  const mealType = typeof params.mealType === 'string' ? params.mealType : '';
  const kind = typeof params.kind === 'string' ? params.kind : '';
  const place = typeof params.place === 'string' ? params.place : '';
  const points = typeof params.points === 'string' ? parseInt(params.points) || 0 : 0;
  const menuNo = typeof params.menuNo === 'string' ? params.menuNo : ''; // menuNo 추가


  const [usePoints, setUsePoints] = useState(0);
  const [selectedMethod, setSelectedMethod] = useState<'card' | 'CASH'>('card');
  const [orderPrice, setOrderPrice] = useState(0);
  const [isPinModalVisible, setIsPinModalVisible] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isQRModalVisible, setIsQRModalVisible] = useState(false);
  const [pinErrorMessage, setPinErrorMessage] = useState('');

  // 가격 정보 가져오기
  useEffect(() => {
    const fetchPrice = async () => {
      if (!isoDate || !menuNo) return; // date와 menuNo 없으면 실행 X

      try {
        const response = await fetch(`${Config.API_BASE_URL}/api/menus/with-prices/${isoDate}`);
        if (response.ok) {
          const data = await response.json();
          const menus = data.menus || [];

          // 메뉴 리스트에서 menuNo로 매칭되는 메뉴 찾기
          const matchedMenu = menus.find(
            (item: any) => String(item.menu.menuNo) === String(menuNo)
          );

          if (matchedMenu) {
            setOrderPrice(matchedMenu.price || 0);
          } else {
            console.warn('해당 메뉴를 찾을 수 없습니다.');
            setOrderPrice(4500); // 기본 가격
          }
        } else {
          console.error('가격 정보 요청 실패:', response.status);
          setOrderPrice(4500); // 기본 가격
        }
      } catch (error) {
        console.error('가격 정보를 가져오는데 실패했습니다:', error);
        setOrderPrice(4500); // 기본 가격
      }
    };

    fetchPrice();
  }, [isoDate, menuNo]);


  // 계산된 값들
  const expectedReward = useMemo(() => {
    return selectedMethod === 'CASH' ? Math.floor((orderPrice - usePoints) * 0.02) : 0;
  }, [selectedMethod, orderPrice, usePoints]);

  const finalPrice = useMemo(() => {
    return Math.max(0, orderPrice - usePoints);
  }, [orderPrice, usePoints]);



  
  // 💳 실제 결제 처리 함수
  const processPayment = async (pin: string) => {
    try {
      const payload = { 
        userNo: 1, // 사용자 번호 (로그인 정보에서 가져올 예정)
        menuId: parseInt(menuNo) || 1,
        menuType: kind,
        amount: orderPrice,
        paymentMethod: selectedMethod === 'card' ? 'CARD' : 'CASH',
        pin,
        accountNo: selectedMethod === 'CASH' ? '9992453470888242' : '',
        usePoints: usePoints > 0,
        pointAmount: usePoints
      };

      console.log('결제 처리 요청:', payload); // 디버깅용 로그

      const response = await fetch(`${Config.API_BASE_URL}/api/payment/process`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const result = await response.json();
        console.log('결제 처리 성공:', result); // 디버깅용 로그
        // QR 모달 표시
        setIsQRModalVisible(true);
      } else {
        const errorText = await response.text();
        console.error('결제 처리 API 오류:', response.status, errorText);
        Alert.alert('결제 오류', `결제 처리에 실패했습니다. (${response.status})`);
      }
    } catch (error) {
      console.error('결제 처리 중 네트워크 오류:', error);
      Alert.alert('오류', '네트워크 오류가 발생했습니다.');
    }
  };

  // 🛡️ 결제 검증
  const handlePaymentValidation = async (pin: string) => {
    setIsProcessing(true);
    try {
      const payload = { 
        userNo: 1,
        menuId: parseInt(menuNo) || 1,
        menuType: kind,
        amount: orderPrice,
        paymentMethod: selectedMethod === 'card' ? 'CARD' : 'CASH',
        pin,
        accountNo: selectedMethod === 'CASH' ? '9992453470888242' : '',
        usePoints: usePoints > 0,
        pointAmount: usePoints
      };

      const response = await fetch(`${Config.API_BASE_URL}/api/payment/validate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      
      if (response.ok) {
        const result = await response.json();
        console.log('결제 검증 응답:', result); // 디버깅용 로그
        
        if (result.isBalanceSufficient && result.isMenuAvailable) {
          // ✅ 결제 가능 시 바로 결제 처리
          await processPayment(pin);
        } else {
          const errorMessage = result.validationMessage || result.errorCode || '결제할 수 없습니다.';
          Alert.alert('결제 불가', errorMessage);
        }
      } else {
        const errorText = await response.text();
        console.error('결제 검증 API 오류:', response.status, errorText);
        Alert.alert('검증 오류', `결제 검증에 실패했습니다. (${response.status})`);
      }
    } catch (error) {
      Alert.alert('오류', '네트워크 오류가 발생했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  // PIN 번호 입력 처리
  const handlePinSubmit = async (pin: string) => {
    // 1. PIN 번호 검증 먼저 진행
    await verifyPin(pin);
  };

  // 🔐 PIN 번호 검증
  const verifyPin = async (pin: string) => {
    setIsProcessing(true);
    setPinErrorMessage(''); // 검증 시작 시 오류 메시지 초기화
    
    try {
      const payload = {
        userNo: 1,
        pin: pin,
        currentPin: null, // 현재 PIN이 null이면 등록 진행
        operation: "PAYMENT"
      };

      console.log('PIN 검증 요청:', payload);

      const response = await fetch(`${Config.API_BASE_URL}/api/payment/verify-pin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const result = await response.json();
        console.log('PIN 검증 응답:', result);

        if (result.success) {
          // PIN 검증 성공 시 결제 검증 진행
          await handlePaymentValidation(pin);
        } else {
          // PIN 검증 실패 시 errorCode에 따른 처리
          if (result.errorCode === 'PIN_VERIFICATION_FAILED') {
            setPinErrorMessage('비밀번호를 다시 확인해주세요');
            return;
          } else if (result.errorCode === 'PIN_NOT_REGISTERED') {
            // PIN이 등록되지 않은 경우에만 등록 진행
            console.log('PIN 미등록 상태, PIN 등록 진행');
            await registerPin(pin);
          } else if (result.errorCode === 'USER_NOT_FOUND') {
            setPinErrorMessage('사용자 정보를 찾을 수 없습니다');
            return;
          } else {
            // 기타 명확하지 않은 오류의 경우 오류 메시지 표시
            setPinErrorMessage(result.message || '오류가 발생했습니다');
            return;
          }
        }
      } else if (response.status === 404) {
        // 404 오류 시 PIN 등록 진행 (사용자 또는 PIN 정보 없음)
        console.log('404 오류, PIN 등록 진행');
        await registerPin(pin);
      } else {
        // 기타 HTTP 오류 시 오류 메시지 표시
        setPinErrorMessage(`서버 오류가 발생했습니다 (${response.status})`);
        return;
      }
    } catch (error) {
      console.error('PIN 검증 중 네트워크 오류:', error);
      // 네트워크 오류 시 오류 메시지 표시
      setPinErrorMessage('네트워크 연결을 확인해주세요');
      return;
    } finally {
      setIsProcessing(false);
    }
  };

  // 📝 PIN 번호 등록
  const registerPin = async (pin: string) => {
    try {
      const payload = {
        userNo: 1,
        currentPin: null,
        newPin: pin,
        confirmPin: pin
      };

      console.log('PIN 등록 요청:', payload);

      const response = await fetch(`${Config.API_BASE_URL}/api/payment/register-pin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const result = await response.json();
        console.log('PIN 등록 응답:', result);

        if (result.success) {
          // PIN 등록 성공 후 바로 결제 검증 진행 (재검증 없이)
          await handlePaymentValidation(pin);
        } else {
          setPinErrorMessage(result.message || 'PIN 등록에 실패했습니다');
        }
      } else {
        const errorText = await response.text();
        console.error('PIN 등록 API 오류:', response.status, errorText);
        setPinErrorMessage(`PIN 등록에 실패했습니다 (${response.status})`);
      }
    } catch (error) {
      console.error('PIN 등록 중 네트워크 오류:', error);
      setPinErrorMessage('네트워크 연결을 확인해주세요');
    }
  };

  // 결제하기 버튼 클릭
  const handlePayment = () => {
    if (finalPrice <= 0) {
      Alert.alert('오류', '결제 금액이 0원 이하입니다.');
      return;
    }
    
    setPinErrorMessage(''); // 오류 메시지 초기화
    // PIN 번호 입력 모달 표시 (백엔드에서 자동으로 등록/검증 판단)
    setIsPinModalVisible(true);
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>결제</Text>
        <View style={styles.placeholder} />
      </View>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* 주문 내역 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>주문 내역</Text>
          <View style={styles.orderCard}>
            <Image
              source={require('../assets/images/food_sample.jpg')}
              style={styles.foodImage}
              resizeMode="cover"
            />
            <View style={styles.orderInfo}>
              <Text style={styles.placeText}>{place}</Text>
              <Text style={styles.menuName}>{mainName}</Text>
            </View>
          </View>
        </View>

        {/* 보유 포인트 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>보유 포인트</Text>
          <View style={styles.pointsCard}>
            <Text style={styles.pointsText}>{points.toLocaleString()} P</Text>
            <View style={styles.pointsInputContainer}>
              <Text style={styles.pointsInputLabel}>사용할 포인트:</Text>
              <TextInput
                style={styles.pointsInput}
                value={usePoints.toString()}
                onChangeText={(text) => setUsePoints(parseInt(text) || 0)}
                keyboardType="numeric"
                placeholder="0"
                placeholderTextColor="#999"
              />
              <Text style={styles.pointsInputLabel}>P</Text>
            </View>
          </View>
        </View>

        {/* 결제수단 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>결제수단</Text>
          <View style={styles.paymentMethods}>
            <TouchableOpacity 
              style={styles.paymentMethod} 
              onPress={() => setSelectedMethod('card')}
            >
              <Ionicons 
                name={selectedMethod === 'card' ? 'radio-button-on' : 'radio-button-off'} 
                size={18} 
                color="#1BB1E7" 
              />
              <Text style={styles.paymentMethodText}>신용/체크카드</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={styles.paymentMethod} 
              onPress={() => setSelectedMethod('CASH')}
            >
              <Ionicons 
                name={selectedMethod === 'CASH' ? 'radio-button-on' : 'radio-button-off'} 
                size={18} 
                color="#1BB1E7" 
              />
              <Text style={styles.paymentMethodText}>계좌 간편결제</Text>
              <Text style={styles.rewardText}>신한 계좌 결제 시 2% 적립</Text>
            </TouchableOpacity>
            
            {selectedMethod === 'CASH' && (
              <View style={styles.accountInfo}>
                <View style={styles.accountItem}>
                  <Ionicons name="card" size={16} color="#1BB1E7" />
                  <Text style={styles.accountText}>신한은행 110-123456789</Text>
                </View>
              </View>
            )}
          </View>
        </View>

        {/* 주문 금액 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>주문 금액</Text>
          <View style={styles.priceCard}>
            <View style={styles.priceRow}>
              <Text style={styles.priceLabel}>상품 금액</Text>
              <Text style={styles.priceValue}>{orderPrice.toLocaleString()}원</Text>
            </View>
            <View style={styles.priceRow}>
              <Text style={styles.priceLabel}>포인트 사용</Text>
              <Text style={styles.priceValue}>-{usePoints.toLocaleString()}원</Text>
            </View>
            <View style={[styles.priceRow, styles.finalPriceRow]}>
              <Text style={styles.finalPriceLabel}>최종 결제 금액</Text>
              <Text style={styles.finalPriceValue}>{finalPrice.toLocaleString()}원</Text>
            </View>
            {expectedReward > 0 && (
              <View style={styles.rewardContainer}>
                <Text style={styles.rewardText}>예상 적립 {expectedReward.toLocaleString()} P</Text>
              </View>
            )}
          </View>
        </View>

        {/* 결제하기 버튼 */}
        <TouchableOpacity
          style={[styles.paymentButton, isProcessing && styles.paymentButtonDisabled]}
          onPress={handlePayment}
          disabled={isProcessing}
        >
          <Text style={styles.paymentButtonText}>
            {isProcessing ? '처리 중...' : '결제하기'}
          </Text>
        </TouchableOpacity>
      </ScrollView>

      {/* PIN 번호 입력 모달 */}
      {Platform.OS === 'web' ? (
        <WebPaymentPinModal
          visible={isPinModalVisible}
          onClose={() => {
            setIsPinModalVisible(false);
            setPinErrorMessage(''); // 모달 닫을 때 오류 메시지 초기화
          }}
          onPinSubmit={handlePinSubmit}
          isRegistration={false}
          errorMessage={pinErrorMessage}
          setErrorMessage={setPinErrorMessage}
        />
      ) : (
        <NativePaymentPinModal
          visible={isPinModalVisible}
          onClose={() => {
            setIsPinModalVisible(false);
            setPinErrorMessage(''); // 모달 닫을 때 오류 메시지 초기화
          }}
          onPinSubmit={handlePinSubmit}
          isRegistration={false}
          errorMessage={pinErrorMessage}
          setErrorMessage={setPinErrorMessage}
        />
      )}

      

             {/* QR코드 모달 */}
       <QRCodeModal
         visible={isQRModalVisible}
         onClose={() => setIsQRModalVisible(false)}
         isoDate={isoDate}
         mainName={mainName}
         mealType={mealType}
       />
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
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    marginBottom: 12,
  },
  orderCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  foodImage: {
    width: 80,
    height: 80,
    borderRadius: 8,
    marginRight: 16,
  },
  orderInfo: {
    flex: 1,
  },
  placeText: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  menuName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  pointsCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  pointsText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1BB1E7',
    marginBottom: 12,
  },
  pointsInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  pointsInputLabel: {
    fontSize: 14,
    color: '#666',
    marginRight: 8,
  },
  pointsInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 6,
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 16,
    color: '#333',
    minWidth: 80,
    textAlign: 'center',
  },
  paymentMethods: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  paymentMethod: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  paymentMethodText: {
    fontSize: 16,
    color: '#333',
    marginLeft: 12,
    flex: 1,
  },
  rewardText: {
    fontSize: 12,
    color: '#4CAF50',
    fontWeight: '600',
    marginLeft: 8,
  },
  accountInfo: {
    marginLeft: 30,
    marginTop: 8,
  },
  accountItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  accountText: {
    fontSize: 14,
    color: '#333',
    marginLeft: 8,
    fontWeight: '500',
  },
  priceCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  priceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  priceLabel: {
    fontSize: 14,
    color: '#666',
  },
  priceValue: {
    fontSize: 14,
    color: '#333',
    fontWeight: '500',
  },
  finalPriceRow: {
    borderTopWidth: 1,
    borderTopColor: '#f0f0f0',
    marginTop: 8,
    paddingTop: 12,
  },
  finalPriceLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  finalPriceValue: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1BB1E7',
  },
  rewardContainer: {
    marginTop: 8,
    alignItems: 'center',
  },
  registerPinButton: {
    backgroundColor: '#6c757d',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  registerPinButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  paymentButton: {
    backgroundColor: PRIMARY_COLOR,
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 4,
  },
  paymentButtonDisabled: {
    backgroundColor: '#ccc',
  },
  paymentButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '700',
  },
  // PIN 모달 스타일
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#fff',
    borderRadius: 20,
    width: '90%',
    maxWidth: 400,
    padding: 20,
  },
  modalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  closeButton: {
    padding: 8,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
  },
  pinSection: {
    alignItems: 'center',
  },
  pinLabel: {
    fontSize: 16,
    color: '#666',
    marginBottom: 20,
    textAlign: 'center',
  },
     pinDisplay: {
     flexDirection: 'row',
     marginBottom: 30,
     gap: 30,
     justifyContent: 'center',
   },
  pinDot: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#ddd',
    backgroundColor: 'transparent',
  },
  pinDotFilled: {
    backgroundColor: '#1BB1E7',
    borderColor: '#1BB1E7',
  },
  keypad: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: 10,
    marginBottom: 20,
  },
  keypadButton: {
    width: 70,
    height: 70,
    borderRadius: 35,
    backgroundColor: '#f8f9fa',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e9ecef',
  },
  keypadEmpty: {
    width: 70,
    height: 70,
  },
  keypadText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  submitButton: {
    backgroundColor: '#e9ecef',
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 24,
    minWidth: 120,
  },
  submitButtonActive: {
    backgroundColor: '#1BB1E7',
  },
  submitButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#999',
    textAlign: 'center',
  },
  submitButtonTextActive: {
    color: '#fff',
  },
  // 웹 전용 모달 스타일
  webModalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  webModalContent: {
    backgroundColor: '#fff',
    borderRadius: 20,
    width: '90%',
    maxWidth: 400,
    padding: 20,
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.2)',
  },
  // QR 모달 스타일
  qrModalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  qrModalContent: {
    backgroundColor: '#fff',
    borderRadius: 20,
    width: '90%',
    maxWidth: 400,
    padding: 20,
    alignItems: 'center',
  },
  qrModalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
    width: '100%',
  },
  qrModalTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
  },
  qrContent: {
    alignItems: 'center',
    marginBottom: 20,
  },
  qrSubtitle: {
    fontSize: 16,
    color: '#333',
    marginBottom: 20,
    textAlign: 'center',
  },
  qrCodeContainer: {
    width: 200,
    height: 200,
    borderRadius: 10,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  qrCodeImage: {
    width: '100%',
    height: '100%',
  },
  paymentInfo: {
    width: '100%',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  infoLabel: {
    fontSize: 14,
    color: '#666',
  },
  infoValue: {
    fontSize: 14,
    color: '#333',
    fontWeight: '500',
  },
  confirmButton: {
    backgroundColor: PRIMARY_COLOR,
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 24,
    minWidth: 120,
  },
  confirmButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
    textAlign: 'center',
  },
  // 오류 메시지 스타일
  errorMessage: {
    color: '#e74c3c',
    fontSize: 14,
    fontWeight: '500',
    textAlign: 'center',
    marginBottom: 15,
    minHeight: 20,
  },
});