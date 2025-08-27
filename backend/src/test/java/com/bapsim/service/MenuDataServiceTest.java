package com.bapsim.service;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.repository.FoodRepository;
import com.bapsim.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuDataServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private MenuDataService menuDataService;

    private Map<String, Object> sampleMenuData;
    private Map<String, Object> sampleFoodData;

    @BeforeEach
    void setUp() {
        // 테스트용 메뉴 데이터 생성
        sampleMenuData = Map.of(
            "menuId", "20250825_A_1",
            "kind", "A",
            "mealType", "한식",
            "isSignature", true,
            "soldOut", false,
            "cafeNo", 1,
            "menuDate", "2025-08-25",
            "food", Map.of(
                "menuName", "우렁된장찌개",
                "kcal", 450,
                "allergy", 2,
                "allergyInfo", "대두(된장, 두부), 갑각류(우렁이)",
                "category", "한식",
                "content", "우렁이와 두부가 들어간 된장찌개"
            )
        );

        sampleFoodData = (Map<String, Object>) sampleMenuData.get("food");
    }

    @Test
    void createMenuFromData_ShouldCreateValidMenu() {
        // When
        Menus menu = menuDataService.createMenuFromData(sampleMenuData, "admin");

        // Then
        assertNotNull(menu);
        assertEquals("20250825_A_1", menu.getMenuId());
        assertEquals("A", menu.getKind());
        assertEquals("한식", menu.getMealType());
        assertTrue(menu.getIsSignature());
        assertFalse(menu.getSoldOut());
        assertEquals(1L, menu.getCafeNo());
        assertEquals(LocalDate.of(2025, 8, 25), menu.getMenuDate());
        assertEquals("admin", menu.getCreatedId());
        assertEquals("admin", menu.getUpdatedId());
        assertNotNull(menu.getCreatedAt());
        assertNotNull(menu.getUpdatedAt());
    }

    @Test
    void createFoodFromData_ShouldCreateValidFood() {
        // When
        Food food = menuDataService.createFoodFromData(sampleFoodData, "20250825_A_1");

        // Then
        assertNotNull(food);
        assertEquals("20250825_A_1", food.getMenuId());
        assertEquals("우렁된장찌개", food.getMenuName());
        assertEquals(450L, food.getKcal());
        assertEquals(2L, food.getAllergy());
        assertEquals("대두(된장, 두부), 갑각류(우렁이)", food.getAllergyInfo());
        assertEquals("한식", food.getCategory());
        assertEquals("우렁이와 두부가 들어간 된장찌개", food.getContent());
    }

    @Test
    void createMenuFromData_WithNullValues_ShouldHandleGracefully() {
        // Given
        Map<String, Object> incompleteData = Map.of(
            "menuId", "20250825_A_2",
            "kind", "A",
            "mealType", "한식"
            // 다른 필드들은 누락
        );

        // When
        Menus menu = menuDataService.createMenuFromData(incompleteData, "admin");

        // Then
        assertNotNull(menu);
        assertEquals("20250825_A_2", menu.getMenuId());
        assertEquals("A", menu.getKind());
        assertEquals("한식", menu.getMealType());
        assertFalse(menu.getIsSignature()); // 기본값
        assertFalse(menu.getSoldOut()); // 기본값
        assertNull(menu.getCafeNo()); // 기본값
        assertNull(menu.getResNo()); // 기본값
    }

    @Test
    void createFoodFromData_WithNullValues_ShouldHandleGracefully() {
        // Given
        Map<String, Object> incompleteFoodData = Map.of(
            "menuName", "테스트메뉴"
            // 다른 필드들은 누락
        );

        // When
        Food food = menuDataService.createFoodFromData(incompleteFoodData, "20250825_A_2");

        // Then
        assertNotNull(food);
        assertEquals("20250825_A_2", food.getMenuId());
        assertEquals("테스트메뉴", food.getMenuName());
        assertEquals(0L, food.getKcal()); // 기본값
        assertEquals(0L, food.getAllergy()); // 기본값
        assertEquals("한식", food.getCategory()); // 기본값
        assertEquals("", food.getContent()); // 기본값
        assertNull(food.getAllergyInfo()); // 기본값
    }

    @Test
    void loadMenuDataFromJson_ShouldSaveMenuAndFood() throws IOException {
        // Given
        when(menuRepository.save(any(Menus.class))).thenReturn(new Menus());
        when(foodRepository.save(any(Food.class))).thenReturn(new Food());

        // When
        menuDataService.loadMenuDataFromJson("data/menus/cafeteria_menus.json");

        // Then
        verify(menuRepository, atLeastOnce()).save(any(Menus.class));
        verify(foodRepository, atLeastOnce()).save(any(Food.class));
    }

    @Test
    void loadMenuDataFromJson_WithInvalidPath_ShouldThrowException() {
        // When & Then
        assertThrows(IOException.class, () -> {
            menuDataService.loadMenuDataFromJson("invalid/path.json");
        });
    }

    @Test
    void parseDate_WithValidDate_ShouldReturnLocalDate() {
        // Given
        String validDate = "2025-08-25";

        // When
        LocalDate result = menuDataService.parseDate(validDate);

        // Then
        assertEquals(LocalDate.of(2025, 8, 25), result);
    }

    @Test
    void parseDate_WithInvalidDate_ShouldReturnNull() {
        // Given
        String invalidDate = "invalid-date";

        // When
        LocalDate result = menuDataService.parseDate(invalidDate);

        // Then
        assertNull(result);
    }

    @Test
    void getStringValue_WithExistingKey_ShouldReturnValue() {
        // Given
        Map<String, Object> data = Map.of("testKey", "testValue");

        // When
        String result = menuDataService.getStringValue(data, "testKey");

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void getStringValue_WithNonExistingKey_ShouldReturnDefault() {
        // Given
        Map<String, Object> data = Map.of("existingKey", "existingValue");

        // When
        String result = menuDataService.getStringValue(data, "nonExistingKey", "defaultValue");

        // Then
        assertEquals("defaultValue", result);
    }

    @Test
    void getLongValue_WithExistingKey_ShouldReturnValue() {
        // Given
        Map<String, Object> data = Map.of("testKey", 123);

        // When
        Long result = menuDataService.getLongValue(data, "testKey");

        // Then
        assertEquals(123L, result);
    }

    @Test
    void getLongValue_WithNonExistingKey_ShouldReturnDefault() {
        // Given
        Map<String, Object> data = Map.of("existingKey", 456);

        // When
        Long result = menuDataService.getLongValue(data, "nonExistingKey", 999L);

        // Then
        assertEquals(999L, result);
    }

    @Test
    void getBooleanValue_WithExistingKey_ShouldReturnValue() {
        // Given
        Map<String, Object> data = Map.of("testKey", true);

        // When
        Boolean result = menuDataService.getBooleanValue(data, "testKey");

        // Then
        assertTrue(result);
    }

    @Test
    void getBooleanValue_WithNonExistingKey_ShouldReturnDefault() {
        // Given
        Map<String, Object> data = Map.of("existingKey", false);

        // When
        Boolean result = menuDataService.getBooleanValue(data, "nonExistingKey", true);

        // Then
        assertTrue(result);
    }
}
