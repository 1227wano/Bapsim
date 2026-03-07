package com.bapsim.dto;

import com.bapsim.entity.Cafeterias;
import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import lombok.Getter;

@Getter
public class SpecificMenuResponse {
    private final String menuName;
    private final String category;
    private final String photoPath;
    private final String mealType;
    private final Boolean isSignature;
    private final String buildName;

    public SpecificMenuResponse(Menus menu) {
        Food food = menu.getFood();
        Cafeterias cafeteria = menu.getCafeteria();

        this.menuName = food.getMenuName();
        this.category = food.getCategory();
        this.photoPath = food.getPhotoPath();
        this.mealType = menu.getMealType();
        this.isSignature = menu.getIsSignature();
        this.buildName = cafeteria != null ? cafeteria.getBuildName() : null;
    }
}
