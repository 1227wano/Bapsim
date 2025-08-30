package com.bapsim.service;

import com.bapsim.dto.*;
import com.bapsim.entity.*;
import com.bapsim.repository.*;
import com.bapsim.util.SsafyApiHeaderGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private MenuPriceRepository menuPriceRepository;
    
    @Autowired
    private SsafyApiService ssafyApiService;
    
    @Autowired
    private SsafyApiHeaderGenerator headerGenerator;
    
    @Autowired
    private MealTicketService mealTicketService;
    
    @Autowired
    private PointService pointService;
    
    /**
     * 결제 전 검증
     */
    public PaymentValidationDto validatePayment(PaymentRequestDto requestDto) {
        // 0. 계좌번호 검증 (SSAFY API 연동 필수)
        if (requestDto.getAccountNo() == null || requestDto.getAccountNo().trim().isEmpty()) {
            log.error("계좌번호가 제공되지 않았습니다: userNo={}", requestDto.getUserNo());
            return PaymentValidationDto.menuUnavailable(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), "알 수 없는 메뉴", "계좌번호가 제공되지 않았습니다. SSAFY API 연동을 위해 계좌번호가 필요합니다.");
        }
        
        // 1. 사용자 존재 여부 확인
        Optional<Member> memberOpt = memberRepository.findById(requestDto.getUserNo());
        if (memberOpt.isEmpty()) {
            return PaymentValidationDto.menuUnavailable(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), "알 수 없는 메뉴", "사용자를 찾을 수 없습니다");
        }
        
        Member member = memberOpt.get();
        
        // 2. 메뉴 존재 여부 및 가격 확인
        Optional<Menus> menuOpt = menuRepository.findById(requestDto.getMenuId());
        if (menuOpt.isEmpty()) {
            return PaymentValidationDto.menuUnavailable(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), "알 수 없는 메뉴", "메뉴를 찾을 수 없습니다");
        }
        
        Menus menu = menuOpt.get();
        
        // 3. 메뉴 가격 조회 (MenuPrice 테이블만 사용)
        // requestDto.getMenuType()을 kind로 사용하여 MenuPrice 테이블에서 직접 조회
        Optional<MenuPrice> menuPriceOpt = menuPriceRepository.findCurrentPrice(
            requestDto.getMenuType(),  // 사용자 요청의 menuType (A, B, C, D, E)
            java.time.LocalDate.now()
        );
        
        if (menuPriceOpt.isEmpty()) {
            log.warn("메뉴 가격 정보를 찾을 수 없음: menuId={}, menuType={}, date={}", 
                    requestDto.getMenuId(), requestDto.getMenuType(), java.time.LocalDate.now());
            
            // 디버깅을 위해 사용 가능한 메뉴 타입들 조회
            List<MenuPrice> availablePrices = menuPriceRepository.findAllCurrentPrices(java.time.LocalDate.now());
            log.info("현재 사용 가능한 메뉴 타입들: {}", 
                    availablePrices.stream()
                        .map(mp -> mp.getKind() + "(" + mp.getMealType() + ")")
                        .collect(java.util.stream.Collectors.toList()));
            
            return PaymentValidationDto.menuUnavailable(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), "메뉴 타입 " + requestDto.getMenuType(), 
                    "해당 메뉴 타입의 가격 정보를 찾을 수 없습니다. 요청한 메뉴 타입: " + requestDto.getMenuType());
        }
        
        MenuPrice menuPrice = menuPriceOpt.get();
        
        // 4. 요청된 금액과 실제 메뉴 가격 비교 (사용자가 금액을 입력한 경우)
        Integer requestedAmount = requestDto.getAmount();
        Integer actualPrice = menuPrice.getPrice().intValue();
        
        if (requestedAmount != null && !requestedAmount.equals(actualPrice)) {
            log.warn("요청된 금액과 실제 메뉴 가격이 다름: 요청={}, 실제={}, userNo={}, menuType={}", 
                    requestedAmount, actualPrice, requestDto.getUserNo(), requestDto.getMenuType());
            return PaymentValidationDto.menuUnavailable(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), "메뉴 타입 " + requestDto.getMenuType(), 
                    "요청된 금액(" + requestedAmount + "원)과 메뉴 가격(" + actualPrice + "원)이 일치하지 않습니다");
        }
        
        // 5. 잔액 확인 (실제 잔액은 SSAFY API에서 조회해야 함)
        Integer userBalance = getCurrentBalance(member.getUserNo());
        
        // 6. 포인트 사용 여부에 따른 검증
        if (requestDto.getUsePoints() != null && requestDto.getUsePoints()) {
            // 포인트 사용 시
            Integer pointBalance = pointService.getPointBalance(requestDto.getUserNo());
            Integer pointAmount = requestDto.getPointAmount() != null ? requestDto.getPointAmount() : 0;
            
            // 포인트 잔액 확인
            if (pointBalance < pointAmount) {
                String menuName = menuPrice.getMealType() != null ? 
                    menuPrice.getMealType() : "메뉴 타입 " + requestDto.getMenuType();
                
                return PaymentValidationDto.insufficientPoints(requestDto.getUserNo(), requestDto.getMenuId(), 
                        requestDto.getMenuType(), menuName, actualPrice, pointBalance, pointAmount);
            }
            
            // 포인트 차감 후 최종 결제 금액 계산
            Integer finalAmount = Math.max(0, actualPrice - pointAmount);
            
            // 최종 결제 금액에 대한 잔액 확인
            if (userBalance < finalAmount) {
                String menuName = menuPrice.getMealType() != null ? 
                    menuPrice.getMealType() : "메뉴 타입 " + requestDto.getMenuType();
                
                return PaymentValidationDto.insufficientBalance(requestDto.getUserNo(), requestDto.getMenuId(), 
                        requestDto.getMenuType(), menuName, finalAmount, userBalance);
            }
            
            // 검증 성공 (포인트 사용)
            String menuName = menuPrice.getMealType() != null ? 
                menuPrice.getMealType() : "메뉴 타입 " + requestDto.getMenuType();
            
            return PaymentValidationDto.successWithPoints(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), menuName, actualPrice, userBalance, 
                    pointAmount, finalAmount, pointBalance);
        } else {
            // 기존 로직 (포인트 미사용)
            if (userBalance < actualPrice) {
                String menuName = menuPrice.getMealType() != null ? 
                    menuPrice.getMealType() : "메뉴 타입 " + requestDto.getMenuType();
                
                return PaymentValidationDto.insufficientBalance(requestDto.getUserNo(), requestDto.getMenuId(), 
                        requestDto.getMenuType(), menuName, actualPrice, userBalance);
            }
            
            // 검증 성공 (포인트 미사용)
            String menuName = menuPrice.getMealType() != null ? 
                menuPrice.getMealType() : "메뉴 타입 " + requestDto.getMenuType();
            
            return PaymentValidationDto.success(requestDto.getUserNo(), requestDto.getMenuId(), 
                    requestDto.getMenuType(), menuName, actualPrice, userBalance);
        }
    }
    
    /**
     * PIN 검증
     */
    public boolean verifyPin(PinVerificationDto pinDto) {
        Optional<Member> memberOpt = memberRepository.findById(pinDto.getUserNo());
        if (memberOpt.isEmpty()) {
            return false;
        }
        
        Member member = memberOpt.get();
        return member.getUserPin().equals(pinDto.getPin());
    }
    
    /**
     * PIN 등록 (최초 등록)
     */
    public PinRegistrationResponseDto registerPin(PinRegistrationDto pinDto) {
        try {
            // 1. 사용자 존재 여부 확인
            Optional<Member> memberOpt = memberRepository.findById(pinDto.getUserNo());
            if (memberOpt.isEmpty()) {
                return PinRegistrationResponseDto.userNotFound();
            }
            
            Member member = memberOpt.get();
            
            // 2. PIN이 이미 등록되어 있는지 확인
            if (member.getUserPin() != null && !member.getUserPin().isEmpty()) {
                return PinRegistrationResponseDto.failure("PIN_ALREADY_EXISTS", "PIN이 이미 등록되어 있습니다. PIN 수정을 이용해주세요.");
            }
            
            // 3. 새로운 PIN과 확인 PIN 일치 여부 확인
            if (!pinDto.isPinMatching()) {
                return PinRegistrationResponseDto.pinMismatch();
            }
            
            // 4. 새로운 PIN 유효성 검증 (4자리 숫자)
            if (pinDto.getNewPin() == null || !pinDto.getNewPin().matches("^[0-9]{4}$")) {
                return PinRegistrationResponseDto.failure("INVALID_PIN_FORMAT", "PIN은 4자리 숫자여야 합니다");
            }
            
            // 5. PIN 등록
            member.setUserPin(pinDto.getNewPin());
            member.setUpdatedAt(LocalDateTime.now());
            member.setUpdatedId("system");
            
            memberRepository.save(member);
            
            return PinRegistrationResponseDto.pinRegistered(member.getUserNo());
            
        } catch (Exception e) {
            return PinRegistrationResponseDto.failure("PIN_REGISTRATION_ERROR", "PIN 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PIN 수정
     */
    public PinRegistrationResponseDto updatePin(PinRegistrationDto pinDto) {
        try {
            // 1. 사용자 존재 여부 확인
            Optional<Member> memberOpt = memberRepository.findById(pinDto.getUserNo());
            if (memberOpt.isEmpty()) {
                return PinRegistrationResponseDto.userNotFound();
            }
            
            Member member = memberOpt.get();
            
            // 2. 현재 PIN 확인
            if (member.getUserPin() == null || member.getUserPin().isEmpty()) {
                return PinRegistrationResponseDto.failure("PIN_NOT_REGISTERED", "등록된 PIN이 없습니다. PIN 등록을 이용해주세요.");
            }
            
            if (!member.getUserPin().equals(pinDto.getCurrentPin())) {
                return PinRegistrationResponseDto.currentPinError();
            }
            
            // 3. 새로운 PIN과 확인 PIN 일치 여부 확인
            if (!pinDto.isPinMatching()) {
                return PinRegistrationResponseDto.pinMismatch();
            }
            
            // 4. PIN 변경 여부 확인
            if (!pinDto.isPinDifferent()) {
                return PinRegistrationResponseDto.noPinChange();
            }
            
            // 5. PIN 수정
            member.setUserPin(pinDto.getNewPin());
            member.setUpdatedAt(LocalDateTime.now());
            member.setUpdatedId("system");
            
            memberRepository.save(member);
            
            return PinRegistrationResponseDto.pinUpdated(member.getUserNo());
            
        } catch (Exception e) {
            return PinRegistrationResponseDto.failure("PIN_UPDATE_ERROR", "PIN 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PIN 삭제 (PIN 초기화)
     */
    public PinRegistrationResponseDto deletePin(Long userNo, String currentPin) {
        try {
            // 1. 사용자 존재 여부 확인
            Optional<Member> memberOpt = memberRepository.findById(userNo);
            if (memberOpt.isEmpty()) {
                return PinRegistrationResponseDto.userNotFound();
            }
            
            Member member = memberOpt.get();
            
            // 2. 현재 PIN 확인
            if (member.getUserPin() == null || member.getUserPin().isEmpty()) {
                return PinRegistrationResponseDto.failure("PIN_NOT_REGISTERED", "등록된 PIN이 없습니다.");
            }
            
            if (!member.getUserPin().equals(currentPin)) {
                return PinRegistrationResponseDto.currentPinError();
            }
            
            // 3. PIN 삭제 (null로 설정)
            member.setUserPin(null);
            member.setUpdatedAt(LocalDateTime.now());
            member.setUpdatedId("system");
            
            memberRepository.save(member);
            
            return PinRegistrationResponseDto.success(userNo, "PIN이 성공적으로 삭제되었습니다");
            
        } catch (Exception e) {
            return PinRegistrationResponseDto.failure("PIN_DELETION_ERROR", "PIN 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PIN 등록 상태 확인
     */
    public boolean isPinRegistered(Long userNo) {
        Optional<Member> memberOpt = memberRepository.findById(userNo);
        if (memberOpt.isEmpty()) {
            return false;
        }
        
        Member member = memberOpt.get();
        return member.getUserPin() != null && !member.getUserPin().isEmpty();
    }
    
    /**
     * 현재 날짜 기준으로 사용 가능한 메뉴 가격 정보 조회
     */
    public List<MenuPrice> getAvailableMenuPrices(java.time.LocalDate date) {
        try {
            return menuPriceRepository.findAllCurrentPrices(date);
        } catch (Exception e) {
            log.error("메뉴 가격 정보 조회 중 오류 발생: date={}", date, e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 메뉴 타입에 따른 현재 가격 조회
     */
    public Integer getMenuPriceByType(String menuType) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            // 메뉴 타입(kind)으로 가격 조회 (mealType은 null로 설정하여 모든 mealType에 대해 조회)
            List<MenuPrice> prices = menuPriceRepository.findCurrentPricesByKind(menuType, today);
            
            if (!prices.isEmpty()) {
                // 첫 번째 가격 반환 (가장 최근에 유효한 가격)
                return prices.get(0).getPrice().intValue();
            }
            
            log.warn("메뉴 타입 {}에 대한 가격 정보를 찾을 수 없음", menuType);
            return null;
            
        } catch (Exception e) {
            log.error("메뉴 타입 {}의 가격 조회 중 오류 발생", menuType, e);
            return null;
        }
    }
    
    /**
     * 결제 처리
     */
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        try {
            // 1. 결제 전 검증
            PaymentValidationDto validation = validatePayment(requestDto);
            if (!validation.getIsBalanceSufficient() || !validation.getIsMenuAvailable()) {
                return PaymentResponseDto.failure(validation.getErrorCode(), validation.getValidationMessage());
            }
            
            // 2. PIN 검증
            PinVerificationDto pinDto = new PinVerificationDto(requestDto.getUserNo(), requestDto.getPin(), null, "PAYMENT");
            if (!verifyPin(pinDto)) {
                return PaymentResponseDto.pinVerificationFailed();
            }
            
            // 3. 포인트 사용 시 포인트 차감
            if (requestDto.getUsePoints() != null && requestDto.getUsePoints() && 
                requestDto.getPointAmount() != null && requestDto.getPointAmount() > 0) {
                try {
                    pointService.usePoints(requestDto.getUserNo(), requestDto.getPointAmount(), 
                        "식권 구매", String.format("메뉴 타입 %s 구매 시 포인트 차감", requestDto.getMenuType()));
                    log.info("포인트 차감 완료: userNo={}, pointAmount={}", requestDto.getUserNo(), requestDto.getPointAmount());
                } catch (Exception e) {
                    log.error("포인트 차감 중 오류 발생: userNo={}, pointAmount={}", requestDto.getUserNo(), requestDto.getPointAmount(), e);
                    return PaymentResponseDto.failure("POINT_DEDUCTION_ERROR", "포인트 차감 중 오류가 발생했습니다");
                }
            }
            
            // 4. 결제 엔티티 생성 (최종 결제 금액으로)
            Payment payment = createPaymentEntity(requestDto);
            if (validation.getFinalAmount() != null) {
                payment.setAmount(validation.getFinalAmount());
            }
            
            // 5. SSAFY 출금 API 호출
            String ssafyTransactionId = callSsafyWithdrawalApi(requestDto);
            if (ssafyTransactionId == null) {
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return PaymentResponseDto.failure("SSAFY_API_ERROR", "SSAFY 출금 API 호출에 실패했습니다");
            }
            
                         // 6. 결제 완료 처리
             payment.setSsafyTransactionId(ssafyTransactionId);
             payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
             payment.setPinVerified(true);
             payment.setUpdatedAt(LocalDateTime.now());
             payment.setUpdatedId("system");
             
             Payment savedPayment = paymentRepository.save(payment);
             
                // 7. 포인트 적립 (최종 결제 금액의 2%)
                try {
                    log.info("결제 완료 후 포인트 적립 시작: paymentId={}, amount={}", savedPayment.getPaymentId(), savedPayment.getAmount());
                    Integer earnedPoints = pointService.earnPointsFromPayment(savedPayment.getUserNo(), savedPayment.getPaymentId(), savedPayment.getAmount());
                    log.info("포인트 적립 완료: paymentId={}, earnedPoints={}", savedPayment.getPaymentId(), earnedPoints);
                } catch (Exception e) {
                    log.error("포인트 적립 중 오류 발생: paymentId={}", savedPayment.getPaymentId(), e);
                    // 포인트 적립 실패는 결제 성공에 영향을 주지 않음
                }
                
                // 8. 식권 자동 발행
                try {
                    log.info("결제 완료 후 식권 발행 시작: paymentId={}", savedPayment.getPaymentId());
                    MealTicket ticket = mealTicketService.issueTicketAfterPayment(savedPayment.getPaymentId());
                    log.info("식권 발행 완료: paymentId={}, ticketId={}", savedPayment.getPaymentId(), ticket != null ? ticket.getTicketId() : "null");
                } catch (Exception e) {
                    log.error("식권 발행 중 오류 발생: paymentId={}", savedPayment.getPaymentId(), e);
                    // 식권 발행 실패는 결제 성공에 영향을 주지 않음
                }
            
                         // 9. 성공 응답 생성
             // menuName은 MenuPrice 테이블의 mealType을 사용
             String menuName = getMenuNameFromMenuPrice(savedPayment.getMenuType());
             if (menuName == null || menuName.isEmpty()) {
                 menuName = "메뉴 타입 " + savedPayment.getMenuType();
             }
             
             // 포인트 사용 여부에 따른 응답 생성
             if (requestDto.getUsePoints() != null && requestDto.getUsePoints() && 
                 requestDto.getPointAmount() != null && requestDto.getPointAmount() > 0) {
                 return PaymentResponseDto.successWithPoints(
                     savedPayment.getPaymentId(),
                     savedPayment.getUserNo(),
                     savedPayment.getMenuId(),
                     savedPayment.getMenuType(),
                     menuName,
                     validation.getMenuPrice(), // 원래 메뉴 가격
                     savedPayment.getAmount(),   // 최종 결제 금액
                     savedPayment.getPaymentStatus(),
                     savedPayment.getPaymentMethod(),
                     savedPayment.getTransactionId(),
                     savedPayment.getSsafyTransactionId(),
                     requestDto.getPointAmount()
                 );
             } else {
                 return PaymentResponseDto.success(
                     savedPayment.getPaymentId(),
                     savedPayment.getUserNo(),
                     savedPayment.getMenuId(),
                     savedPayment.getMenuType(),
                     menuName,
                     savedPayment.getAmount(),
                     savedPayment.getPaymentStatus(),
                     savedPayment.getPaymentMethod(),
                     savedPayment.getTransactionId(),
                     savedPayment.getSsafyTransactionId()
                 );
             }
            
        } catch (Exception e) {
            return PaymentResponseDto.failure("PAYMENT_PROCESS_ERROR", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 내역 조회
     */
    public List<PaymentHistoryDto> getPaymentHistory(Long userNo, LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments;
        
        if (startDate != null && endDate != null) {
            payments = paymentRepository.findByUserNoAndCreatedAtBetweenOrderByCreatedAtDesc(userNo, startDate, endDate);
        } else {
            payments = paymentRepository.findByUserNoOrderByCreatedAtDesc(userNo);
        }
        
        return payments.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 결제 취소
     */
    public PaymentResponseDto cancelPayment(Long paymentId, Long userNo) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return PaymentResponseDto.failure("PAYMENT_NOT_FOUND", "결제 내역을 찾을 수 없습니다");
        }
        
        Payment payment = paymentOpt.get();
        
        // 권한 확인
        if (!payment.getUserNo().equals(userNo)) {
            return PaymentResponseDto.failure("UNAUTHORIZED", "해당 결제를 취소할 권한이 없습니다");
        }
        
        // 취소 가능 여부 확인
        if (!payment.canBeCancelled()) {
            return PaymentResponseDto.failure("CANCEL_NOT_ALLOWED", "해당 결제는 취소할 수 없습니다");
        }
        
        // SSAFY 환불 API 호출 (필요시)
        // TODO: SSAFY 환불 API 구현
        
        // 결제 상태 변경
        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedId("system");
        
        paymentRepository.save(payment);
        
                 // menuName은 MenuPrice 테이블의 mealType을 사용
         String menuName = getMenuNameFromMenuPrice(payment.getMenuType());
         if (menuName == null || menuName.isEmpty()) {
             menuName = "메뉴 타입 " + payment.getMenuType();
         }
         
         return PaymentResponseDto.success(
             payment.getPaymentId(),
             payment.getUserNo(),
             payment.getMenuId(),
             payment.getMenuType(),
             menuName,
             payment.getAmount(),
             payment.getPaymentStatus(),
             payment.getPaymentMethod(),
             payment.getTransactionId(),
             payment.getSsafyTransactionId()
         );
    }
    
    // Private helper methods
    
    private Payment createPaymentEntity(PaymentRequestDto requestDto) {
        Payment payment = new Payment();
        payment.setUserNo(requestDto.getUserNo());
        payment.setMenuId(requestDto.getMenuId());
        payment.setMenuType(requestDto.getMenuType());
        
        // 메뉴 타입에 따른 실제 가격으로 설정 (사용자 입력 금액이 아닌)
        Integer actualPrice = getMenuPriceByType(requestDto.getMenuType());
        payment.setAmount(actualPrice != null ? actualPrice : requestDto.getAmount());
        
        // 포인트 사용 정보 설정
        payment.setUsePoints(requestDto.getUsePoints() != null ? requestDto.getUsePoints() : false);
        payment.setPointAmount(requestDto.getPointAmount() != null ? requestDto.getPointAmount() : 0);
        payment.setOriginalPrice(actualPrice != null ? actualPrice : requestDto.getAmount());
        
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setPinVerified(false);
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedId("system");
        payment.setUpdatedId("system");
        
        return payment;
    }
    
    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String callSsafyWithdrawalApi(PaymentRequestDto requestDto) {
        try {
            // SSAFY 출금 API 호출
            String accountNo = requestDto.getAccountNo();
            if (accountNo == null || accountNo.trim().isEmpty()) {
                log.error("계좌번호가 제공되지 않았습니다: userNo={}", requestDto.getUserNo());
                return null;
            }
            
            // 포인트 사용 여부에 따른 최종 결제 금액 계산
            Integer actualAmount;
            if (requestDto.getUsePoints() != null && requestDto.getUsePoints() && 
                requestDto.getPointAmount() != null && requestDto.getPointAmount() > 0) {
                // 포인트 사용 시: 원래 가격 - 포인트 금액
                Integer originalPrice = getMenuPriceByType(requestDto.getMenuType());
                actualAmount = Math.max(0, originalPrice - requestDto.getPointAmount());
            } else {
                // 포인트 미사용 시: 원래 가격
                actualAmount = requestDto.getAmount() != null ? 
                    requestDto.getAmount() : getMenuPriceByType(requestDto.getMenuType());
            }
            
            if (actualAmount == null) {
                log.error("메뉴 타입 {}에 대한 가격을 찾을 수 없습니다: userNo={}", 
                        requestDto.getMenuType(), requestDto.getUserNo());
                return null;
            }
            
            WithdrawalResponse response = ssafyApiService.withdrawMoney(
                accountNo, 
                new java.math.BigDecimal(actualAmount), 
                "학식 결제 - " + requestDto.getMenuType() + " 메뉴"
            );
            
            if (response != null && response.getHeader() != null && "H0000".equals(response.getHeader().getResponseCode())) {
                log.info("SSAFY 출금 API 성공: responseCode={}", response.getHeader().getResponseCode());
                
                // 거래 고유 번호 반환
                if (response.getTransactionInfo() != null && response.getTransactionInfo().getTransactionUniqueNo() != null) {
                    log.info("SSAFY 출금 API 거래 고유 번호: {}", response.getTransactionInfo().getTransactionUniqueNo());
                    return response.getTransactionInfo().getTransactionUniqueNo();
                }
                // 거래 고유 번호가 없으면 응답 코드 반환
                log.warn("SSAFY 출금 API 거래 고유 번호가 없음, 응답 코드 반환: {}", response.getHeader().getResponseCode());
                return response.getHeader().getResponseCode();
            } else {
                if (response != null && response.getHeader() != null) {
                    log.error("SSAFY 출금 API 응답 실패: responseCode={}, response={}", 
                            response.getHeader().getResponseCode(), response);
                } else {
                    log.error("SSAFY 출금 API 응답이 null이거나 헤더가 없음: {}", response);
                }
                return null;
            }
            
        } catch (Exception e) {
            log.error("SSAFY 출금 API 호출 중 오류 발생", e);
            return null;
        }
    }
    
    private Integer getCurrentBalance(Long userNo) {
        try {
            // SSAFY 잔액 조회 API 호출
            // TODO: Member 엔티티에서 계좌번호 조회 필요
            // 임시로 하드코딩된 계좌번호 사용 (실제로는 Member 테이블에서 조회)
            String accountNo = "9992453470888242"; // 테스트용 계좌번호
            
            log.info("SSAFY 잔액 조회 시작: userNo={}, accountNo={}", userNo, accountNo);
            
            BalanceInquiryResponse response = ssafyApiService.inquireBalance(accountNo);
            
            // 전체 응답 로깅 (디버깅용)
            log.info("SSAFY API 전체 응답: {}", response);
            
            if (response != null && response.getHeader() != null && "H0000".equals(response.getHeader().getResponseCode())) {
                log.info("SSAFY API 헤더 성공: responseCode={}", response.getHeader().getResponseCode());
                
                // SSAFY API 응답에서 잔액 추출
                if (response.getAccountInfo() != null && response.getAccountInfo().getAccountBalance() != null) {
                    log.info("SSAFY API 계좌 정보 존재: accountBalance={}", response.getAccountInfo().getAccountBalance());
                    try {
                        Integer balance = Integer.parseInt(response.getAccountInfo().getAccountBalance());
                        log.info("SSAFY 잔액 조회 성공: userNo={}, balance={}", userNo, balance);
                        return balance;
                    } catch (NumberFormatException e) {
                        log.error("잔액 파싱 오류: userNo={}, rawBalance={}", userNo, response.getAccountInfo().getAccountBalance(), e);
                        return 0;
                    }
                } else {
                    log.warn("SSAFY API 응답에 잔액 정보가 없음: userNo={}, response={}", userNo, response);
                    if (response.getAccountInfo() != null) {
                        log.warn("AccountInfo는 존재하지만 AccountBalance가 null: {}", response.getAccountInfo());
                    } else {
                        log.warn("AccountInfo 자체가 null");
                    }
                    return 0;
                }
            } else {
                if (response != null && response.getHeader() != null) {
                    log.error("SSAFY 잔액 조회 API 응답 실패: userNo={}, responseCode={}, response={}", 
                            userNo, response.getHeader().getResponseCode(), response);
                } else {
                    log.error("SSAFY 잔액 조회 API 응답이 null이거나 헤더가 없음: userNo={}, response={}", userNo, response);
                }
                return 0;
            }
            
        } catch (Exception e) {
            log.error("SSAFY 잔액 조회 API 호출 중 오류 발생: userNo={}", userNo, e);
            return 0;
        }
    }
    
    private String getMenuName(Long menuId) {
        Optional<Menus> menuOpt = menuRepository.findById(menuId);
        return menuOpt.map(Menus::getMenuName).orElse("알 수 없는 메뉴");
    }
    
    /**
     * MenuPrice 테이블에서 menuType(kind)에 해당하는 mealType을 조회
     */
    private String getMenuNameFromMenuPrice(String menuType) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            Optional<MenuPrice> menuPriceOpt = menuPriceRepository.findCurrentPrice(menuType, today);
            
            if (menuPriceOpt.isPresent()) {
                MenuPrice menuPrice = menuPriceOpt.get();
                return menuPrice.getMealType();
            }
            
            log.warn("메뉴 타입 {}에 대한 mealType을 찾을 수 없음", menuType);
            return null;
            
        } catch (Exception e) {
            log.error("메뉴 타입 {}의 mealType 조회 중 오류 발생", menuType, e);
            return null;
        }
    }
    
    /**
     * 계좌번호로 현재 잔액 조회 (SSAFY API)
     */
    public Integer getCurrentBalanceForAccount(String accountNo) {
        try {
            log.info("SSAFY 잔액 조회 시작: accountNo={}", accountNo);
            
            BalanceInquiryResponse response = ssafyApiService.inquireBalance(accountNo);
            
            if (response != null && response.getHeader() != null && "H0000".equals(response.getHeader().getResponseCode())) {
                // SSAFY API 응답에서 잔액 추출
                if (response.getAccountInfo() != null && response.getAccountInfo().getAccountBalance() != null) {
                    try {
                        Integer balance = Integer.parseInt(response.getAccountInfo().getAccountBalance());
                        log.info("SSAFY 잔액 조회 성공: accountNo={}, balance={}", accountNo, balance);
                        return balance;
                    } catch (NumberFormatException e) {
                        log.error("잔액 파싱 오류: accountNo={}, rawBalance={}", accountNo, response.getAccountInfo().getAccountBalance(), e);
                        return 0;
                    }
                } else {
                    log.warn("SSAFY API 응답에 잔액 정보가 없음: accountNo={}, response={}", accountNo, response);
                    return 0;
                }
            } else {
                log.error("SSAFY 잔액 조회 API 응답 실패: accountNo={}, response={}", accountNo, response);
                return 0;
            }
            
        } catch (Exception e) {
            log.error("SSAFY 잔액 조회 API 호출 중 오류 발생: accountNo={}", accountNo, e);
            return 0;
        }
    }
    
         private PaymentHistoryDto convertToHistoryDto(Payment payment) {
         // menuName은 MenuPrice 테이블의 mealType을 사용
         String menuName = getMenuNameFromMenuPrice(payment.getMenuType());
         if (menuName == null || menuName.isEmpty()) {
             menuName = "메뉴 타입 " + payment.getMenuType();
         }
         
         return PaymentHistoryDto.builder()
                 .paymentId(payment.getPaymentId())
                 .userNo(payment.getUserNo())
                 .menuId(payment.getMenuId())
                 .menuType(payment.getMenuType())
                 .menuName(menuName)
                 .amount(payment.getAmount())
                 .paymentStatus(payment.getPaymentStatus())
                 .paymentMethod(payment.getPaymentMethod())
                 .pinVerified(payment.getPinVerified())
                 .transactionId(payment.getTransactionId())
                 .ssafyTransactionId(payment.getSsafyTransactionId())
                 .createdAt(payment.getCreatedAt())
                 .updatedAt(payment.getUpdatedAt())
                 .build();
     }
}
