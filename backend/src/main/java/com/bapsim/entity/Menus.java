package com.bapsim.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List; // 변경점: List 임포트
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Menus")
public class Menus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MENU_NO")
    private Long menuNo;

    // 변경점 1: menuId 필드 제거. PK인 menuNo를 기준으로 관계를 맺음
    // @Column(name = "MENU_ID", length = 100, nullable = false)
    // private String menuId;

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

    // 변경점 2: Food와의 관계 수정.
    // Food 엔티티의 'menu' 필드에 의해 매핑됨을 명시합니다 (mappedBy).
    // Menus는 이제 관계의 주인이 아닙니다.
    @OneToOne(mappedBy = "menu", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"menu", "hibernateLazyInitializer"})
    private Food food;

    // 변경점 3: MenuPrice와의 관계 추가 (OneToMany)
    // 하나의 메뉴는 여러 가격 정보를 가질 수 있습니다.
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"menu", "hibernateLazyInitializer"})
    private List<MenuPrice> menuPrices;

    // Constructors
    public Menus() {}

    // Getters and Setters
    public Long getMenuNo() {
        return menuNo;
    }

    public void setMenuNo(Long menuNo) {
        this.menuNo = menuNo;
    }

    // menuId Getters and Setters 제거

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

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    // 변경점 4: menuPrices에 대한 Getter, Setter 추가
    public List<MenuPrice> getMenuPrices() {
        return menuPrices;
    }

    public void setMenuPrices(List<MenuPrice> menuPrices) {
        this.menuPrices = menuPrices;
    }
}