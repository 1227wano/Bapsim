package com.bapsim.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    // MenuPrice:Menus = 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_NO")
    @JsonIgnoreProperties({"menuPrice", "hibernateLazyInitializer"})
    private Menus menu;


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

    public Menus getMenu() {
        return menu;
    }

    public void setMenu(Menus menu) {
        this.menu = menu;
    }
}