package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Point_history")
public class PointHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POINT_ID")
    private Long pointId;
    
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;
    
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    
    @Column(name = "POINT_CHANGED")
    private Integer pointChanged;
    
    @Column(name = "REASON", length = 50)
    private String reason;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", insertable = false, updatable = false)
    private Member user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_ID", insertable = false, updatable = false)
    private Payment payment;
    
    // Constructors
    public PointHistory() {}
    
    // Getters and Setters
    public Long getPointId() {
        return pointId;
    }
    
    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }
    
    public Long getUserNo() {
        return userNo;
    }
    
    public void setUserNo(Long userNo) {
        this.userNo = userNo;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public Integer getPointChanged() {
        return pointChanged;
    }
    
    public void setPointChanged(Integer pointChanged) {
        this.pointChanged = pointChanged;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Member getUser() {
        return user;
    }
    
    public void setUser(Member user) {
        this.user = user;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
