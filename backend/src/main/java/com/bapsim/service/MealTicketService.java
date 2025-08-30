package com.bapsim.service;

import com.bapsim.entity.MealTicket;
import com.bapsim.entity.Payment;
import com.bapsim.repository.MealTicketRepository;
import com.bapsim.repository.PaymentRepository;
import com.bapsim.repository.MenuPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 식권 발행 및 관리 서비스 (단순화)
 * 결제 완료 → 식권 발급 → 사용/미사용 상태만 관리
 */
@Service
@Transactional
public class MealTicketService {
    
    private static final Logger log = LoggerFactory.getLogger(MealTicketService.class);
    
    @Autowired
    private MealTicketRepository mealTicketRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private MenuPriceRepository menuPriceRepository;
    
    /**
     * 결제 완료 후 식권 자동 발행
     * @param paymentId 결제 ID
     * @return 발행된 식권
     */
    public MealTicket issueTicketAfterPayment(Long paymentId) {
        try {
            log.info("결제 완료 후 식권 발행 시작: paymentId={}", paymentId);
            
            // 1. 결제 정보 조회
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                log.error("결제 정보를 찾을 수 없음: paymentId={}", paymentId);
                throw new RuntimeException("결제 정보를 찾을 수 없습니다");
            }
            
            Payment payment = paymentOpt.get();
            
            // 2. 결제 상태 확인
            if (!payment.isCompleted()) {
                log.error("결제가 완료되지 않음: paymentId={}, status={}", paymentId, payment.getPaymentStatus());
                throw new RuntimeException("결제가 완료되지 않았습니다");
            }
            
            // 3. 이미 발행된 식권이 있는지 확인
            Optional<MealTicket> existingTicket = mealTicketRepository.findByPaymentId(paymentId);
            if (existingTicket.isPresent()) {
                log.warn("이미 발행된 식권이 존재함: paymentId={}, ticketId={}", paymentId, existingTicket.get().getTicketId());
                return existingTicket.get();
            }
            
            // 4. 메뉴 가격 정보 조회
            Integer menuPrice = getMenuPriceByType(payment.getMenuType());
            if (menuPrice == null) {
                log.error("메뉴 가격 정보를 찾을 수 없음: menuType={}", payment.getMenuType());
                throw new RuntimeException("메뉴 가격 정보를 찾을 수 없습니다");
            }
            
            // 5. 식권 생성
            MealTicket mealTicket = createMealTicket(payment, menuPrice);
            
            // 6. 식권 저장
            MealTicket savedTicket = mealTicketRepository.save(mealTicket);
            
            log.info("식권 발행 완료: ticketId={}, paymentId={}", savedTicket.getTicketId(), paymentId);
            
            return savedTicket;
            
        } catch (Exception e) {
            log.error("식권 발행 중 오류 발생: paymentId={}", paymentId, e);
            throw new RuntimeException("식권 발행에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 식권 생성
     */
    private MealTicket createMealTicket(Payment payment, Integer amount) {
        // 1. 메뉴명 조회
        String menuName = getMenuNameByType(payment.getMenuType());
        
        // 2. 발행일시 설정
        LocalDateTime issuedAt = LocalDateTime.now();
        
        // 3. MealTicket 엔티티 생성
        MealTicket mealTicket = MealTicket.builder()
                .paymentId(payment.getPaymentId())
                .userNo(payment.getUserNo())
                .menuType(payment.getMenuType())
                .menuName(menuName)
                .amount(amount)
                .issuedAt(issuedAt)
                .isUsed(false)
                .createdId("system")
                .updatedId("system")
                .build();
        
        return mealTicket;
    }
    
    /**
     * 메뉴 타입별 가격 조회
     */
    private Integer getMenuPriceByType(String menuType) {
        try {
            var today = java.time.LocalDate.now();
            var menuPriceOpt = menuPriceRepository.findCurrentPrice(menuType, today);
            return menuPriceOpt.map(mp -> mp.getPrice().intValue()).orElse(null);
        } catch (Exception e) {
            log.error("메뉴 가격 조회 중 오류 발생: menuType={}", menuType, e);
            return null;
        }
    }
    
    /**
     * 메뉴 타입별 메뉴명 조회
     */
    private String getMenuNameByType(String menuType) {
        try {
            var today = java.time.LocalDate.now();
            var menuPriceOpt = menuPriceRepository.findCurrentPrice(menuType, today);
            return menuPriceOpt.map(mp -> mp.getMealType()).orElse("메뉴 타입 " + menuType);
        } catch (Exception e) {
            log.error("메뉴명 조회 중 오류 발생: menuType={}", menuType, e);
            return "메뉴 타입 " + menuType;
        }
    }
    
    /**
     * 식권 사용 처리
     * @param ticketId 식권 ID
     * @param location 사용 위치 (식당명)
     * @return 사용 처리된 식권
     */
    public MealTicket useTicket(Long ticketId, String location) {
        try {
            log.info("식권 사용 처리 시작: ticketId={}, location={}", ticketId, location);
            
            // 1. 식권 조회
            Optional<MealTicket> ticketOpt = mealTicketRepository.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                log.error("식권을 찾을 수 없음: ticketId={}", ticketId);
                throw new RuntimeException("식권을 찾을 수 없습니다");
            }
            
            MealTicket ticket = ticketOpt.get();
            
            // 2. 식권 사용 가능 여부 확인
            if (ticket.getIsUsed()) {
                log.error("이미 사용된 식권: ticketId={}", ticketId);
                throw new RuntimeException("이미 사용된 식권입니다");
            }
            
            // 3. 식권 사용 처리
            ticket.use(location);
            MealTicket updatedTicket = mealTicketRepository.save(ticket);
            
            log.info("식권 사용 처리 완료: ticketId={}", ticketId);
            
            return updatedTicket;
            
        } catch (Exception e) {
            log.error("식권 사용 처리 중 오류 발생: ticketId={}", ticketId, e);
            throw new RuntimeException("식권 사용 처리에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 조회 메서드들
    
    /**
     * 식권 ID로 식권 조회
     */
    public Optional<MealTicket> findById(Long ticketId) {
        return mealTicketRepository.findById(ticketId);
    }
    
    /**
     * 사용자별 식권 목록 조회
     */
    public List<MealTicket> findByUserNo(Long userNo) {
        return mealTicketRepository.findByUserNoOrderByCreatedAtDesc(userNo);
    }
    
    /**
     * 사용자별 사용 가능한 식권 목록 조회 (미사용)
     */
    public List<MealTicket> findAvailableTicketsByUser(Long userNo) {
        return mealTicketRepository.findByUserNoAndIsUsedFalseOrderByCreatedAtDesc(userNo);
    }
    
    /**
     * 사용자별 사용된 식권 목록 조회 (사용됨)
     */
    public List<MealTicket> findUsedTicketsByUser(Long userNo) {
        return mealTicketRepository.findByUserNoAndIsUsedTrueOrderByUsedAtDesc(userNo);
    }
    
    /**
     * 결제 ID로 식권 조회
     */
    public Optional<MealTicket> findByPaymentId(Long paymentId) {
        return mealTicketRepository.findByPaymentId(paymentId);
    }
    
    // 통계 기능 제거 (단순화)
    
    /**
     * 별도 트랜잭션으로 식권 발행 처리
     * 결제 트랜잭션과 분리하여 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MealTicket issueTicketAfterPaymentInNewTransaction(Long paymentId) {
        return issueTicketAfterPayment(paymentId);
    }
}
