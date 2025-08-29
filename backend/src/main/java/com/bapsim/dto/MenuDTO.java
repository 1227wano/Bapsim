package com.bapsim.dto;

import java.time.LocalDate;

public class MenuDTO {
    private Long menuNo;
    private String menuName;
    private Long price; // 추가된 필드
    private String category;
    private Long kcal;
    private String photoPath;
    private Boolean isSignature;
    private Boolean soldOut;
    private String mealType;
    private LocalDate menuDate;

    // 기본 생성자
    public MenuDTO() {}

    // JPQL 등에서 프로젝션을 사용하기 위한 생성자
    public MenuDTO(Long menuNo, String menuName, Long price, String category, Long kcal, String photoPath, Boolean isSignature, Boolean soldOut, String mealType, LocalDate menuDate) {
        this.menuNo = menuNo;
        this.menuName = menuName;
        this.price = price;
        this.category = category;
        this.kcal = kcal;
        this.photoPath = photoPath;
        this.isSignature = isSignature;
        this.soldOut = soldOut;
        this.mealType = mealType;
        this.menuDate = menuDate;
    }

    // Getters and Setters
    public Long getMenuNo() {
        return menuNo;
    }

    public void setMenuNo(Long menuNo) {
        this.menuNo = menuNo;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getKcal() {
        return kcal;
    }

    public void setKcal(Long kcal) {
        this.kcal = kcal;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Boolean getIsSignature() {
        return isSignature;
    }

    public void setIsSignature(Boolean isSignature) {
        this.isSignature = isSignature;
    }

    public Boolean getSoldOut() {
        return soldOut;
    }

    public void setSoldOut(Boolean soldOut) {
        this.soldOut = soldOut;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDate getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(LocalDate menuDate) {
        this.menuDate = menuDate;
    }
}
