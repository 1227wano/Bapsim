package com.bapsim.service;

import com.bapsim.dto.RandomMenuResponse;
import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.repository.MenuRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class MenuDataService {

    private static final Logger logger = LoggerFactory.getLogger(MenuDataService.class);

    @Autowired
    private MenuRepository menuRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void loadMenuDataFromJson(String jsonFilePath) {
        try {
            logger.info("메뉴 데이터 JSON 파일 로딩 시작: {}", jsonFilePath);
            ClassPathResource resource = new ClassPathResource(jsonFilePath);
            InputStream inputStream = resource.getInputStream();
            List<Map<String, Object>> menuDataList = objectMapper.readValue(
                inputStream, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            logger.info("JSON 파일에서 {} 개의 메뉴 데이터를 읽었습니다.", menuDataList.size());

            for (Map<String, Object> menuData : menuDataList) {
                saveMenuData(menuData);
            }
            logger.info("메뉴 데이터 저장 완료");
        } catch (IOException e) {
            logger.error("JSON 파일 읽기 실패: {}", jsonFilePath, e);
            throw new RuntimeException("메뉴 데이터 로딩 실패", e);
        }
    }

    private void saveMenuData(Map<String, Object> menuData) {
        try {
            // 1. Create Menus entity
            Menus menu = createMenuFromData(menuData);

            // 2. Create Food entity if it exists
            if (menuData.containsKey("food")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> foodData = (Map<String, Object>) menuData.get("food");
                Food food = createFoodFromData(foodData);

                // 3. Link entities together
                menu.setFood(food);
                food.setMenu(menu);
            }

            // 4. Save the parent entity (Menus). Food will be saved automatically by cascade.
            Menus savedMenu = menuRepository.save(menu);
            logger.debug("메뉴 저장 완료: {}", savedMenu.getMenuNo());

        } catch (Exception e) {
            logger.error("메뉴 데이터 저장 실패: {}", menuData, e);
        }
    }

    @Transactional(readOnly = true)
    public RandomMenuResponse getRandomMenu() {
        long count = menuRepository.count();
        if (count == 0) {
            throw new RuntimeException("No menus available");
        }
        int randomIndex = new Random().nextInt((int) count);
        Pageable pageable = PageRequest.of(randomIndex, 1);
        Page<Menus> menuPage = menuRepository.findAll(pageable);
        if (menuPage.hasContent()) {
            Menus menu = menuPage.getContent().get(0);
            return new RandomMenuResponse(menu);
        } else {
            throw new RuntimeException("Menu not found at random index.");
        }
    }

    private Menus createMenuFromData(Map<String, Object> menuData) {
        Menus menu = new Menus();
        menu.setKind(getStringValue(menuData, "kind", "A"));
        menu.setMealType(getStringValue(menuData, "mealType", "점심"));
        menu.setIsSignature(getBooleanValue(menuData, "isSignature", false));
        menu.setSoldOut(getBooleanValue(menuData, "soldOut", false));

        if (menuData.containsKey("cafeNo")) {
            menu.setCafeNo(getLongValue(menuData, "cafeNo"));
        } else if (menuData.containsKey("resNo")) {
            menu.setResNo(getLongValue(menuData, "resNo"));
        }

        if (menuData.containsKey("menuDate")) {
            String dateStr = getStringValue(menuData, "menuDate");
            try {
                LocalDate menuDate = LocalDate.parse(dateStr);
                menu.setMenuDate(menuDate);
            } catch (Exception e) {
                logger.warn("날짜 파싱 실패: {}", dateStr);
            }
        }

        String currentUser = "system";
        LocalDateTime now = LocalDateTime.now();
        menu.setCreatedId(currentUser);
        menu.setCreatedAt(now);
        menu.setUpdatedId(currentUser);
        menu.setUpdatedAt(now);

        return menu;
    }

    private Food createFoodFromData(Map<String, Object> foodData) {
        Food food = new Food();
        food.setMenuName(getStringValue(foodData, "menuName"));
        food.setKcal(getLongValue(foodData, "kcal", 0L));
        food.setAllergy(getLongValue(foodData, "allergy", 0L));
        food.setCategory(getStringValue(foodData, "category", "한식"));
        food.setContent(getStringValue(foodData, "content", ""));

        if (foodData.containsKey("photoPath")) {
            food.setPhotoPath(getStringValue(foodData, "photoPath"));
        }
        if (foodData.containsKey("allergyInfo")) {
            food.setAllergyInfo(getStringValue(foodData, "allergyInfo"));
        }
        return food;
    }

    // Utility methods
    private String getStringValue(Map<String, Object> data, String key) { return getStringValue(data, key, ""); }
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) { Object value = data.get(key); return value != null ? value.toString() : defaultValue; }
    private Long getLongValue(Map<String, Object> data, String key) { return getLongValue(data, key, 0L); }
    private Long getLongValue(Map<String, Object> data, String key, Long defaultValue) { Object value = data.get(key); if (value == null) return defaultValue; if (value instanceof Number) { return ((Number) value).longValue(); } try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return defaultValue; } }
    private Boolean getBooleanValue(Map<String, Object> data, String key) { return getBooleanValue(data, key, false); }
    private Boolean getBooleanValue(Map<String, Object> data, String key, Boolean defaultValue) { Object value = data.get(key); if (value == null) return defaultValue; if (value instanceof Boolean) { return (Boolean) value; } return Boolean.parseBoolean(value.toString()); }
}