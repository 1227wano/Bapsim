package com.bapsim.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 식권 엔티티 (단순화)
 * 결제 완료 후 발행되는 디지털 식권
 * 사용/미사용 상태만 관리
 */
@Entity
@Table(name = "meal_tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MealTicket {
    
    /**
     * 식권 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;
    
    /**
     * 결제 ID (Payment 엔티티와 연관관계)
     */
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    /**
     * 사용자 ID
     */
    @Column(name = "user_no", nullable = false)
    private Long userNo;
    
    /**
     * 메뉴 타입 (A, B, C, D, E)
     */
    @Column(name = "menu_type", nullable = false, length = 10)
    private String menuType;
    
    /**
     * 메뉴명
     */
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;
    
    /**
     * 식권 금액
     */
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    /**
     * 발행일시
     */
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    /**
     * 사용 여부 (true: 사용됨, false: 미사용)
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;
    
    /**
     * 사용일시 (사용 시 설정)
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    /**
     * 사용 위치 (식당명)
     */
    @Column(name = "used_location", length = 100)
    private String usedLocation;
    
    /**
     * 생성일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 생성자 ID
     */
    @Column(name = "created_id", length = 50)
    private String createdId;
    
    /**
     * 수정자 ID
     */
    @Column(name = "updated_id", length = 50)
    private String updatedId;
    
    /**
     * 식권이 사용 가능한지 확인
     */
    public boolean isAvailable() {
        return !isUsed;
    }
    
    /**
     * 식권을 사용 처리
     */
    public void use(String location) {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
        this.usedLocation = location;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * PrePersist: 엔티티 저장 전 실행
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isUsed == null) {
            isUsed = false;
        }
        if (createdId == null) {
            createdId = "system";
        }
        if (updatedId == null) {
            updatedId = "system";
        }
    }
    
    /**
     * PreUpdate: 엔티티 수정 전 실행
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedId == null) {
            updatedId = "system";
        }
    }
}
