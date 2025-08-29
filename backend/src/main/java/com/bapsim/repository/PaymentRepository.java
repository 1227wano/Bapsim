package com.bapsim.repository;

import com.bapsim.entity.Payment;
import com.bapsim.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // 사용자별 결제 내역 조회
    List<Payment> findByUserNoOrderByCreatedAtDesc(Long userNo);
    
    // 사용자별 특정 상태의 결제 내역 조회
    List<Payment> findByUserNoAndPaymentStatusOrderByCreatedAtDesc(Long userNo, PaymentStatus status);
    
    // 메뉴별 결제 내역 조회
    List<Payment> findByMenuIdOrderByCreatedAtDesc(Long menuId);
    
    // 결제 상태별 조회
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    
    // 특정 기간 내 결제 내역 조회
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // 사용자별 특정 기간 내 결제 내역 조회
    List<Payment> findByUserNoAndCreatedAtBetweenOrderByCreatedAtDesc(Long userNo, LocalDateTime startDate, LocalDateTime endDate);
    
    // SSAFY 거래 ID로 결제 조회
    Optional<Payment> findBySsafyTransactionId(String ssafyTransactionId);
    
    // 내부 거래 ID로 결제 조회
    Optional<Payment> findByTransactionId(String transactionId);
    
    // PIN 검증이 완료된 결제 조회
    List<Payment> findByPinVerifiedTrue();
    
    // 메뉴 타입별 결제 내역 조회
    List<Payment> findByMenuTypeOrderByCreatedAtDesc(String menuType);
    
    // 사용자별 메뉴 타입별 결제 내역 조회
    List<Payment> findByUserNoAndMenuTypeOrderByCreatedAtDesc(Long userNo, String menuType);
    
    // 특정 금액 이상의 결제 내역 조회
    List<Payment> findByAmountGreaterThanEqualOrderByAmountDesc(Integer amount);
    
    // 사용자별 총 결제 금액 계산
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userNo = :userNo AND p.paymentStatus = :status")
    Integer sumAmountByUserNoAndStatus(@Param("userNo") Long userNo, @Param("status") PaymentStatus status);
    
    // 사용자별 결제 건수 조회
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userNo = :userNo AND p.paymentStatus = :status")
    Long countByUserNoAndStatus(@Param("userNo") Long userNo, @Param("status") PaymentStatus status);
    
    // 오늘 결제된 총 금액 조회
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE DATE(p.createdAt) = CURDATE() AND p.paymentStatus = :status")
    Integer sumTodayAmountByStatus(@Param("status") PaymentStatus status);
    
    // 특정 메뉴의 총 판매 금액 조회
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.menuId = :menuId AND p.paymentStatus = :status")
    Integer sumAmountByMenuIdAndStatus(@Param("menuId") Long menuId, @Param("status") PaymentStatus status);
    
    // 대기 중인 결제 조회 (PIN 검증 전)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.pinVerified = false")
    List<Payment> findPendingUnverifiedPayments();
    
    // 완료된 결제 중 PIN 검증이 안된 결제 조회 (보안 검증용)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.pinVerified = false")
    List<Payment> findCompletedUnverifiedPayments();
}
