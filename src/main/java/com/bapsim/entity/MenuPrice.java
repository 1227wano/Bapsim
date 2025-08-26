package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Menu_Price")
public class MenuPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRICE_NO")
    private Long priceNo;
    
    @Column(name = "KIND", length = 1, nullable = false)
    private String kind; // A, B, C, D, E
    
    @Column(name = "MEAL_TYPE", length = 100, nullable = false)
    private String mealType; // 점심, 저녁 등
    
    @Column(name = "PRICE", nullable = false)
    private Long price;
    
    @Column(name = "DESCRIPTION", length = 500)
    private String description; // "밥, 국, 반찬" 등
    
    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate effectiveDate; // 적용 시작일
    
    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate; // 적용 종료일 (null이면 계속 적용)
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;
    
    @Column(name = "CREATED_ID", length = 100, nullable = false)
    private String createdId;
    
    @Column(name = "CREATED_AT", nullable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "UPDATED_ID", length = 100, nullable = false)
    private String updatedId;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private java.time.LocalDateTime updatedAt;
    
    // Constructors
    public MenuPrice() {}
    
    // Getters and Setters
    public Long getPriceNo() {
        return priceNo;
    }
    
    public void setPriceNo(Long priceNo) {
        this.priceNo = priceNo;
    }
    
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public Long getPrice() {
        return price;
    }
    
    public void setPrice(Long price) {
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }
    
    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getCreatedId() {
        return createdId;
    }
    
    public void setCreatedId(String createdId) {
        this.createdId = createdId;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedId() {
        return updatedId;
    }
    
    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
