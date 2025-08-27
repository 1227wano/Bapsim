package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Payment")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    
    @Column(name = "USER_NO", nullable = false)
    private Long userNo;
    
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;
    
    @Column(name = "TRANSACTION_ID", length = 100, nullable = false)
    private String transactionId;
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", insertable = false, updatable = false)
    private Member user;
    
    @OneToMany(mappedBy = "payment")
    private List<PointHistory> pointHistories;
    
    // Constructors
    public Payment() {}
    
    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public Long getUserNo() {
        return userNo;
    }
    
    public void setUserNo(Long userNo) {
        this.userNo = userNo;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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
    
    public List<PointHistory> getPointHistories() {
        return pointHistories;
    }
    
    public void setPointHistories(List<PointHistory> pointHistories) {
        this.pointHistories = pointHistories;
    }
}
