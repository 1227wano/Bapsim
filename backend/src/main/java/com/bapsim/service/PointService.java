package com.bapsim.service;

import com.bapsim.entity.Member;
import com.bapsim.entity.PointHistory;
import com.bapsim.repository.MemberRepository;
import com.bapsim.repository.PointHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

/**
 * 포인트 관리 서비스
 * 포인트 적립, 사용, 조회 등을 처리
 */
@Service
@Transactional
public class PointService {
    
    private static final Logger log = LoggerFactory.getLogger(PointService.class);
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    
    /**
     * 결제 완료 후 포인트 적립 (결제 금액의 2%)
     * @param userNo 사용자 번호
     * @param paymentId 결제 ID
     * @param paymentAmount 결제 금액
     * @return 적립된 포인트 수량
     */
    public Integer earnPointsFromPayment(Long userNo, Long paymentId, Integer paymentAmount) {
        try {
            log.info("포인트 적립 시작: userNo={}, paymentId={}, amount={}", userNo, paymentId, paymentAmount);
            
            // 1. 포인트 계산 (결제 금액의 2%)
            Integer earnedPoints = (int)(paymentAmount * 0.02);
            
            if (earnedPoints <= 0) {
                log.warn("적립할 포인트가 없음: userNo={}, amount={}, earnedPoints={}", userNo, paymentAmount, earnedPoints);
                return 0;
            }
            
            // 2. 포인트 적립 처리
            Integer newBalance = earnPoints(userNo, paymentId, earnedPoints, "결제 적립", 
                String.format("결제 금액 %d원의 2%% 적립 (%d포인트)", paymentAmount, earnedPoints));
            
            log.info("포인트 적립 완료: userNo={}, paymentId={}, earnedPoints={}, newBalance={}", 
                userNo, paymentId, earnedPoints, newBalance);
            
            return earnedPoints;
            
        } catch (Exception e) {
            log.error("포인트 적립 중 오류 발생: userNo={}, paymentId={}", userNo, paymentId, e);
            throw new RuntimeException("포인트 적립에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 포인트 적립
     * @param userNo 사용자 번호
     * @param paymentId 결제 ID (선택적)
     * @param points 적립할 포인트
     * @param reason 적립 사유
     * @param description 상세 설명
     * @return 적립 후 포인트 잔액
     */
    public Integer earnPoints(Long userNo, Long paymentId, Integer points, String reason, String description) {
        // 1. 사용자 포인트 잔액 증가
        Member member = memberRepository.findById(userNo)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userNo));
        
        Integer currentBalance = member.getPointBalance() != null ? member.getPointBalance() : 0;
        Integer newBalance = currentBalance + points;
        
        member.setPointBalance(newBalance);
        memberRepository.save(member);
        
        // 2. 포인트 적립 내역 기록
        PointHistory history = PointHistory.earnPoints(userNo, paymentId, points, newBalance, reason, description);
        pointHistoryRepository.save(history);
        
        log.info("포인트 적립 완료: userNo={}, points={}, currentBalance={}, newBalance={}", 
            userNo, points, currentBalance, newBalance);
        
        return newBalance;
    }
    
    /**
     * 포인트 사용
     * @param userNo 사용자 번호
     * @param points 사용할 포인트
     * @param reason 사용 사유
     * @param description 상세 설명
     * @return 사용 후 포인트 잔액
     */
    public Integer usePoints(Long userNo, Integer points, String reason, String description) {
        // 1. 사용자 포인트 잔액 확인
        Member member = memberRepository.findById(userNo)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userNo));
        
        Integer currentBalance = member.getPointBalance() != null ? member.getPointBalance() : 0;
        
        if (currentBalance < points) {
            throw new RuntimeException("포인트가 부족합니다. 현재: " + currentBalance + ", 필요: " + points);
        }
        
        // 2. 포인트 사용 처리
        Integer newBalance = currentBalance - points;
        member.setPointBalance(newBalance);
        memberRepository.save(member);
        
        // 3. 포인트 사용 내역 기록
        PointHistory history = PointHistory.usePoints(userNo, points, newBalance, reason, description);
        pointHistoryRepository.save(history);
        
        log.info("포인트 사용 완료: userNo={}, points={}, currentBalance={}, newBalance={}", 
            userNo, points, currentBalance, newBalance);
        
        return newBalance;
    }
    
    /**
     * 포인트 잔액 조회
     * @param userNo 사용자 번호
     * @return 포인트 잔액
     */
    public Integer getPointBalance(Long userNo) {
        Optional<Member> memberOpt = memberRepository.findById(userNo);
        if (memberOpt.isEmpty()) {
            return 0;
        }
        
        Member member = memberOpt.get();
        return member.getPointBalance() != null ? member.getPointBalance() : 0;
    }
    
    // 포인트 잔액은 Member 테이블의 pointBalance 필드에서 직접 조회
    
    /**
     * 포인트 내역 조회
     * @param userNo 사용자 번호
     * @return 포인트 내역 목록
     */
    public List<PointHistory> getPointHistory(Long userNo) {
        return pointHistoryRepository.findByUserNoOrderByCreatedAtDesc(userNo);
    }
    
    /**
     * 포인트 적립 내역만 조회
     * @param userNo 사용자 번호
     * @return 포인트 적립 내역 목록
     */
    public List<PointHistory> getEarnPointHistory(Long userNo) {
        return pointHistoryRepository.findByUserNoAndPointTypeOrderByCreatedAtDesc(userNo, PointHistory.PointType.EARN);
    }
    
    /**
     * 포인트 사용 내역만 조회
     * @param userNo 사용자 번호
     * @return 포인트 사용 내역 목록
     */
    public List<PointHistory> getUsePointHistory(Long userNo) {
        return pointHistoryRepository.findByUserNoAndPointTypeOrderByCreatedAtDesc(userNo, PointHistory.PointType.USE);
    }
    
    /**
     * 별도 트랜잭션으로 포인트 적립 처리
     * 결제 트랜잭션과 분리하여 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer earnPointsFromPaymentInNewTransaction(Long userNo, Long paymentId, Integer paymentAmount) {
        return earnPointsFromPayment(userNo, paymentId, paymentAmount);
    }
}
