package com.bapsim.entity;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Food")
public class Food {

    // 변경점 1: Food 테이블의 독립적인 PK (foodNo) 추가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOOD_NO")
    private Long foodNo;

    // 변경점 2: menuId 필드 제거. 아래 Menus와의 관계로 대체됩니다.
    // @Id
    // @Column(name = "MENU_ID", length = 100, nullable = false)
    // private String menuId;

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

    // 변경점 3: Menus와의 관계 수정 (OneToOne).
    // 이제 Food 테이블이 관계의 주인이 되어 'MENU_NO'라는 FK를 가집니다.
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MENU_NO", referencedColumnName = "MENU_NO")
    @JsonIgnoreProperties({"food", "hibernateLazyInitializer"})
    private Menus menu;

    // Constructors
    public Food() {}

    // Getters and Setters
    // 변경점 4: 새로 추가된 foodNo의 Getter, Setter
    public Long getFoodNo() {
        return foodNo;
    }

    public void setFoodNo(Long foodNo) {
        this.foodNo = foodNo;
    }

    // menuId의 Getter, Setter 제거

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