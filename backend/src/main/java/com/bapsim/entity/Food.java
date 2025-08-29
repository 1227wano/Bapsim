package com.bapsim.entity;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOOD_NO")
    private Long foodNo;

    @Column(name = "MENU_NAME", length = 100, nullable = false)
    private String menuName;

    @Column(name = "KCAL", nullable = false)
    private Long kcal;

    @Column(name = "ALLERGY", nullable = false)
    private Long allergy;

    @Column(name = "CATEGORY", length = 10, nullable = false)
    private String category;

    @Column(name = "CONTENT", length = 500)
    private String content;

    @Column(name = "PHOTO_PATH", length = 500)
    private String photoPath;

    @Column(name = "ALLERGY_INFO", length = 500)
    private String allergyInfo;

    // Food:Menus = N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_NO")
    @JsonIgnoreProperties({"foods", "hibernateLazyInitializer"})
    private Menus menu;

    // Constructors
    public Food() {}

    // Getters and Setters
    public Long getFoodNo() {
        return foodNo;
    }

    public void setFoodNo(Long foodNo) {
        this.foodNo = foodNo;
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

    public Menus getMenu() {
        return menu;
    }

    public void setMenu(Menus menu) {
        this.menu = menu;
    }
}