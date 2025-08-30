package com.bapsim.repository;

import com.bapsim.entity.MealTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 식권 데이터 접근을 위한 Repository (단순화)
 */
@Repository
public interface MealTicketRepository extends JpaRepository<MealTicket, Long> {
    
    /**
     * 결제 ID로 식권 조회
     */
    Optional<MealTicket> findByPaymentId(Long paymentId);
    
    /**
     * 사용자별 식권 목록 조회 (최신순)
     */
    List<MealTicket> findByUserNoOrderByCreatedAtDesc(Long userNo);
    
    /**
     * 사용자별 사용 가능한 식권 목록 조회 (미사용)
     */
    List<MealTicket> findByUserNoAndIsUsedFalseOrderByCreatedAtDesc(Long userNo);
    
    /**
     * 사용자별 사용된 식권 목록 조회 (사용됨)
     */
    List<MealTicket> findByUserNoAndIsUsedTrueOrderByUsedAtDesc(Long userNo);
    
    /**
     * 메뉴 타입별 식권 목록 조회
     */
    List<MealTicket> findByMenuTypeOrderByCreatedAtDesc(String menuType);
    
    // 개수 조회 메서드들 제거 (단순화)
}
