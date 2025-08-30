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
    
    @Column(name = "MENU_ID", nullable = false)
    private Long menuId;
    
    @Column(name = "MENU_TYPE", length = 10, nullable = false)
    private String menuType; // A, B, C, D, E
    
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;
    
    @Column(name = "PAYMENT_STATUS", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Column(name = "PAYMENT_METHOD", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Column(name = "PIN_VERIFIED", nullable = false)
    private Boolean pinVerified;
    
    @Column(name = "TRANSACTION_ID", length = 100, nullable = false)
    private String transactionId;
    
    @Column(name = "SSAFY_TRANSACTION_ID", length = 100)
    private String ssafyTransactionId;
    
    // 포인트 사용 관련 필드 추가
    @Column(name = "USE_POINTS")
    private Boolean usePoints = false; // 포인트 사용 여부
    
    @Column(name = "POINT_AMOUNT")
    private Integer pointAmount = 0; // 사용된 포인트 금액
    
    @Column(name = "ORIGINAL_PRICE")
    private Integer originalPrice; // 원래 메뉴 가격 (포인트 차감 전)
    
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    @Column(name = "CREATED_ID", length = 50)
    private String createdId;
    
    @Column(name = "UPDATED_ID", length = 50)
    private String updatedId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", insertable = false, updatable = false)
    private Member user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_ID", insertable = false, updatable = false)
    private Menus menu;
    
    @OneToMany(mappedBy = "payment")
    private List<PointHistory> pointHistories;
    
    // MealTicket과의 연관관계는 제거 (단순화)
    
    // Payment Status Enum
    public enum PaymentStatus {
        PENDING,    // 결제 대기
        COMPLETED,  // 결제 완료
        FAILED,     // 결제 실패
        CANCELLED,  // 결제 취소
        REFUNDED    // 환불됨
    }
    
    // Payment Method Enum
    public enum PaymentMethod {
        POINT,      // 포인트 결제
        CARD,       // 카드 결제
        CASH        // 현금 결제
    }
    
    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
        this.pinVerified = false;
    }
    
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
    
    public Long getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    public String getMenuType() {
        return menuType;
    }
    
    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Boolean getPinVerified() {
        return pinVerified;
    }
    
    public void setPinVerified(Boolean pinVerified) {
        this.pinVerified = pinVerified;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getSsafyTransactionId() {
        return ssafyTransactionId;
    }
    
    public void setSsafyTransactionId(String ssafyTransactionId) {
        this.ssafyTransactionId = ssafyTransactionId;
    }
    
    public Boolean getUsePoints() {
        return usePoints;
    }
    
    public void setUsePoints(Boolean usePoints) {
        this.usePoints = usePoints;
    }
    
    public Integer getPointAmount() {
        return pointAmount;
    }
    
    public void setPointAmount(Integer pointAmount) {
        this.pointAmount = pointAmount;
    }
    
    public Integer getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedId() {
        return createdId;
    }
    
    public void setCreatedId(String createdId) {
        this.createdId = createdId;
    }
    
    public String getUpdatedId() {
        return updatedId;
    }
    
    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
    
    public Member getUser() {
        return user;
    }
    
    public void setUser(Member user) {
        this.user = user;
    }
    
    public Menus getMenu() {
        return menu;
    }
    
    public void setMenu(Menus menu) {
        this.menu = menu;
    }
    
    public List<PointHistory> getPointHistories() {
        return pointHistories;
    }
    
    public void setPointHistories(List<PointHistory> pointHistories) {
        this.pointHistories = pointHistories;
    }
    
        // MealTicket getter/setter 제거 (단순화)
    
    // Business Methods
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.createdId == null) {
            this.createdId = "system";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.updatedId == null) {
            this.updatedId = "system";
        }
    }
    
    // Helper Methods
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.paymentStatus);
    }
    
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.paymentStatus);
    }
    
    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.paymentStatus);
    }
    
    public boolean canBeCancelled() {
        return PaymentStatus.PENDING.equals(this.paymentStatus) || 
               PaymentStatus.COMPLETED.equals(this.paymentStatus);
    }
}
