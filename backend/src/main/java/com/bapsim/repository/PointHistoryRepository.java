package com.bapsim.repository;

import com.bapsim.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 포인트 내역 Repository
 */
@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    /**
     * 사용자별 포인트 내역 조회 (최신순)
     */
    List<PointHistory> findByUserNoOrderByCreatedAtDesc(Long userNo);
    
    /**
     * 사용자별 특정 타입의 포인트 내역 조회 (최신순)
     */
    List<PointHistory> findByUserNoAndPointTypeOrderByCreatedAtDesc(Long userNo, String pointType);
    
    /**
     * 결제 ID로 포인트 내역 조회
     */
    List<PointHistory> findByPaymentId(Long paymentId);
    
    /**
     * 사용자별 포인트 적립 내역만 조회 (최신순)
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userNo = :userNo AND ph.pointType = 'EARN' ORDER BY ph.createdAt DESC")
    List<PointHistory> findEarnHistoryByUserNo(@Param("userNo") Long userNo);
    
    /**
     * 사용자별 포인트 사용 내역만 조회 (최신순)
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userNo = :userNo AND ph.pointType = 'USE' ORDER BY ph.createdAt DESC")
    List<PointHistory> findUseHistoryByUserNo(@Param("userNo") Long userNo);
    
    // 포인트 잔액은 Member 테이블의 pointBalance 필드에서 직접 조회하므로
    // 복잡한 쿼리가 필요하지 않음
}
