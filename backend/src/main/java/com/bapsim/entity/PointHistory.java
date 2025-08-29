package com.bapsim.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 포인트 내역 엔티티
 * 포인트 적립, 사용, 만료 등의 내역을 기록
 */
@Entity
@Table(name = "Point_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PointHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POINT_ID")
    private Long pointId;
    
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;
    
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    
    @Column(name = "POINT_TYPE", nullable = false, length = 20)
    private String pointType; // EARN(적립), USE(사용), EXPIRE(만료)
    
    @Column(name = "POINTS", nullable = false)
    private Integer points; // 포인트 수량 (양수: 적립, 음수: 사용/만료)
    
    @Column(name = "BALANCE_AFTER", nullable = false)
    private Integer balanceAfter; // 포인트 변경 후 잔액
    
    @Column(name = "REASON", length = 100)
    private String reason; // 포인트 변경 사유
    
    @Column(name = "DESCRIPTION", length = 200)
    private String description; // 상세 설명
    
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "CREATED_ID", length = 50)
    private String createdId;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", insertable = false, updatable = false)
    private Member user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_ID", insertable = false, updatable = false)
    private Payment payment;
    
    /**
     * 포인트 타입 상수
     */
    public static class PointType {
        public static final String EARN = "EARN";      // 적립
        public static final String USE = "USE";        // 사용
        public static final String EXPIRE = "EXPIRE";  // 만료
    }
    
    /**
     * 포인트 적립 내역 생성
     */
    public static PointHistory earnPoints(Long userNo, Long paymentId, Integer points, 
                                        Integer balanceAfter, String reason, String description) {
        return PointHistory.builder()
                .userNo(userNo)
                .paymentId(paymentId)
                .pointType(PointType.EARN)
                .points(points)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .description(description)
                .createdId("system")
                .build();
    }
    
    /**
     * 포인트 사용 내역 생성
     */
    public static PointHistory usePoints(Long userNo, Integer points, 
                                       Integer balanceAfter, String reason, String description) {
        return PointHistory.builder()
                .userNo(userNo)
                .pointType(PointType.USE)
                .points(-points) // 음수로 기록
                .balanceAfter(balanceAfter)
                .reason(reason)
                .description(description)
                .createdId("system")
                .build();
    }
    
    /**
     * 포인트 만료 내역 생성
     */
    public static PointHistory expirePoints(Long userNo, Integer points, 
                                          Integer balanceAfter, String reason) {
        return PointHistory.builder()
                .userNo(userNo)
                .pointType(PointType.EXPIRE)
                .points(-points) // 음수로 기록
                .balanceAfter(balanceAfter)
                .reason(reason)
                .description("포인트 만료")
                .createdId("system")
                .build();
    }
}
