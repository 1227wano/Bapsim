package com.bapsim.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 변경점: 임포트 추가

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Menu_Price")
public class MenuPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRICE_NO")
    private Long priceNo;

    // ... (기존 필드들은 변경 없음)
    @Column(name = "KIND", length = 1, nullable = false)
    private String kind;

    @Column(name = "MEAL_TYPE", length = 100, nullable = false)
    private String mealType;

    @Column(name = "PRICE", nullable = false)
    private Long price;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

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

    // 변경점 1: Menus와의 관계 추가 (ManyToOne)
    // 여러 가격 정보가 하나의 메뉴에 연결될 수 있습니다.
    // Menu_Price 테이블에 'MENU_NO'라는 FK가 생성됩니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_NO")
    @JsonIgnoreProperties({"menuPrices", "hibernateLazyInitializer"})
    private Menus menu;


    // Constructors
    public MenuPrice() {}

    // Getters and Setters
    // ... (기존 Getter, Setter들은 변경 없음)
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

    // 변경점 2: menu에 대한 Getter, Setter 추가
    public Menus getMenu() {
        return menu;
    }

    public void setMenu(Menus menu) {
        this.menu = menu;
    }
}