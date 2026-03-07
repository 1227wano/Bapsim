package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Menus")
public class Menus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENU_NO")
    private Long menuNo;

<<<<<<< HEAD

=======
>>>>>>> 0e8b4bb207c7bdc1ddda5d81e6733070780fff8f
    @Column(name = "KIND", length = 1, nullable = false)
    private String kind;

    @Column(name = "MEAL_TYPE", length = 100, nullable = false)
    private String mealType;

    @Column(name = "IS_SIGNATURE", nullable = false)
    private Boolean isSignature;

    @Column(name = "CREATED_ID", length = 100, nullable = false)
    private String createdId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_ID", length = 100, nullable = false)
    private String updatedId;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "SOLD_OUT", nullable = false)
    private Boolean soldOut;

    @Column(name = "CAFE_NO", insertable = false, updatable = false)
    private Long cafeNo;

    @Column(name = "RES_NO")
    private Long resNo;

    @Column(name = "MENU_DATE")
    private LocalDate menuDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "CAFE_NO", insertable = false, updatable = false)
    @JsonIgnoreProperties({"menus", "hibernateLazyInitializer"})
    private Cafeterias cafeteria;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "RES_NO", insertable = false, updatable = false)
    @JsonIgnoreProperties({"menus", "hibernateLazyInitializer"})
    private Restaurants restaurant;

    // Menus:Food = 1:N
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"menu", "hibernateLazyInitializer"})
    private List<Food> foods;

    // Menus:MenuPrice = 1:1
    @OneToOne(mappedBy = "menu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"menu", "hibernateLazyInitializer"})
    private MenuPrice menuPrice;

    // Constructors
    public Menus() {}

    // Getters and Setters
    public Long getMenuNo() {
        return menuNo;
    }

    public void setMenuNo(Long menuNo) {
        this.menuNo = menuNo;
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

    public Boolean getIsSignature() {
        return isSignature;
    }

    public void setIsSignature(Boolean isSignature) {
        this.isSignature = isSignature;
    }

    public String getCreatedId() {
        return createdId;
    }

    public void setCreatedId(String createdId) {
        this.createdId = createdId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(String updatedId) {
        this.updatedId = updatedId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getSoldOut() {
        return soldOut;
    }

    public void setSoldOut(Boolean soldOut) {
        this.soldOut = soldOut;
    }

    public Long getCafeNo() {
        return cafeNo;
    }

    public void setCafeNo(Long cafeNo) {
        this.cafeNo = cafeNo;
    }

    public Long getResNo() {
        return resNo;
    }

    public void setResNo(Long resNo) {
        this.resNo = resNo;
    }

    public LocalDate getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(LocalDate menuDate) {
        this.menuDate = menuDate;
    }

    public Cafeterias getCafeteria() {
        return cafeteria;
    }

    public void setCafeteria(Cafeterias cafeteria) {
        this.cafeteria = cafeteria;
    }

    public Restaurants getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurants restaurant) {
        this.restaurant = restaurant;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    public MenuPrice getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(MenuPrice menuPrice) {
        this.menuPrice = menuPrice;
    }
    
    public String getMenuName() {
        if (this.foods != null && !this.foods.isEmpty()) {
            return this.foods.get(0).getMenuName();
        }
        return this.kind + " - " + this.mealType;
    }
}