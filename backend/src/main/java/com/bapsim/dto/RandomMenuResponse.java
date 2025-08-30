package com.bapsim.dto;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import lombok.Getter;

@Getter
public class RandomMenuResponse {
    private final String menuName;
    private final String category;
    private final String photoPath;
    private final String mealType;
    private final Boolean isSignature;

    public RandomMenuResponse(Menus menu) {
        Food food = menu.getFood();
        this.menuName = food.getMenuName();
        this.category = food.getCategory();
        this.photoPath = food.getPhotoPath();
        this.mealType = menu.getMealType();
        this.isSignature = menu.getIsSignature();
    }
}