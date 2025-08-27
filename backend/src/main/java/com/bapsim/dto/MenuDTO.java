package com.bapsim.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MenuDTO {
    private Long menuNo;
    private String menuId;
    private String kind;
    private String mealType;
    private Boolean isSignature;
    private String createdId;
    private LocalDateTime createdAt;
    private String updatedId;
    private LocalDateTime updatedAt;
    private Boolean soldOut;
    private Long cafeNo;
    private Long resNo;
    private LocalDate menuDate;
    
    // Food 정보
    private String menuName;
    private Long kcal;
    private Long allergy;
    private String category;
    private String content;
    private String photoPath;
    private String allergyInfo;
    
    // Cafeteria 정보
    private String buildName;
    private String cafeteriaPhoneNo;
    
    // Restaurant 정보
    private String resName;
    private String address;
    private String restaurantPhoneNo;
    
    // 기본 생성자
    public MenuDTO() {}
    
    // Hibernate 쿼리용 생성자
    public MenuDTO(Long menuNo, String menuId, String kind, String mealType, Boolean isSignature,
                   String createdId, LocalDateTime createdAt, String updatedId, LocalDateTime updatedAt,
                   Boolean soldOut, Long cafeNo, Long resNo, LocalDate menuDate,
                   String menuName, Long kcal, Long allergy, String category, String content, 
                   String photoPath, String allergyInfo,
                   String buildName, Long cafeteriaPhoneNo,
                   String resName, String address, Long restaurantPhoneNo) {
        this.menuNo = menuNo;
        this.menuId = menuId;
        this.kind = kind;
        this.mealType = mealType;
        this.isSignature = isSignature;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
        this.soldOut = soldOut;
        this.cafeNo = cafeNo;
        this.resNo = resNo;
        this.menuDate = menuDate;
        this.menuName = menuName;
        this.kcal = kcal;
        this.allergy = allergy;
        this.category = category;
        this.content = content;
        this.photoPath = photoPath;
        this.allergyInfo = allergyInfo;
        this.buildName = buildName;
        this.cafeteriaPhoneNo = cafeteriaPhoneNo != null ? cafeteriaPhoneNo.toString() : null;
        this.resName = resName;
        this.address = address;
        this.restaurantPhoneNo = restaurantPhoneNo != null ? restaurantPhoneNo.toString() : null;
    }
    
    // Getters and Setters
    public Long getMenuNo() {
        return menuNo;
    }
    
    public void setMenuNo(Long menuNo) {
        this.menuNo = menuNo;
    }
    
    public String getMenuId() {
        return menuId;
    }
    
    public void setMenuId(String menuId) {
        this.menuId = menuId;
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
    
    public String getMenuName() {
        return menuName;
    }
    
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    
    public Long getKcal() {
        return kcal;
    }
    
    public void setKcal(Long kcal) {
        this.kcal = kcal;
    }
    
    public Long getAllergy() {
        return allergy;
    }
    
    public void setAllergy(Long allergy) {
        this.allergy = allergy;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public String getAllergyInfo() {
        return allergyInfo;
    }
    
    public void setAllergyInfo(String allergyInfo) {
        this.allergyInfo = allergyInfo;
    }
    
    public String getBuildName() {
        return buildName;
    }
    
    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }
    
    public String getCafeteriaPhoneNo() {
        return cafeteriaPhoneNo;
    }
    
    public void setCafeteriaPhoneNo(String cafeteriaPhoneNo) {
        this.cafeteriaPhoneNo = cafeteriaPhoneNo;
    }
    
    public String getResName() {
        return resName;
    }
    
    public void setResName(String resName) {
        this.resName = resName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getRestaurantPhoneNo() {
        return restaurantPhoneNo;
    }
    
    public void setRestaurantPhoneNo(String restaurantPhoneNo) {
        this.restaurantPhoneNo = restaurantPhoneNo;
    }
}
