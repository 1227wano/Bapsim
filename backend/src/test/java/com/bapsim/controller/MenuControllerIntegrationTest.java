package com.bapsim.controller;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.entity.MenuPrice;
import com.bapsim.repository.FoodRepository;
import com.bapsim.repository.MenuRepository;
import com.bapsim.repository.MenuPriceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class MenuControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private MenuPriceRepository menuPriceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Menus testMenu;
    private Food testFood;
    private MenuPrice testMenuPrice;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 테스트 데이터 생성 및 저장
        createTestData();
    }

    private void createTestData() {
        // 메뉴 가격 데이터 생성
        testMenuPrice = new MenuPrice();
        testMenuPrice.setKind("A");
        testMenuPrice.setMealType("한식");
        testMenuPrice.setPrice(6000L);
        testMenuPrice.setDescription("밥, 국, 반찬");
        testMenuPrice.setEffectiveDate(LocalDate.of(2025, 8, 25));
        testMenuPrice.setIsActive(true);
        testMenuPrice.setCreatedId("system");
        testMenuPrice.setCreatedAt(LocalDateTime.now());
        testMenuPrice.setUpdatedId("system");
        testMenuPrice.setUpdatedAt(LocalDateTime.now());
        testMenuPrice = menuPriceRepository.save(testMenuPrice);

        // 메뉴 데이터 생성
        testMenu = new Menus();
        testMenu.setMenuId("20250825_A_1");
        testMenu.setKind("A");
        testMenu.setPrice(6000L);
        testMenu.setMealType("한식");
        testMenu.setIsSignature(true);
        testMenu.setCreatedId("admin");
        testMenu.setCreatedAt(LocalDateTime.now());
        testMenu.setUpdatedId("admin");
        testMenu.setUpdatedAt(LocalDateTime.now());
        testMenu.setSoldOut(false);
        testMenu.setCafeNo(1L);
        testMenu.setMenuDate(LocalDate.of(2025, 8, 25));
        testMenu = menuRepository.save(testMenu);

        // 음식 데이터 생성
        testFood = new Food();
        testFood.setMenuId("20250825_A_1");
        testFood.setMenuName("우렁된장찌개");
        testFood.setKcal(450L);
        testFood.setAllergy(2L);
        testFood.setAllergyInfo("대두(된장, 두부), 갑각류(우렁이)");
        testFood.setCategory("한식");
        testFood.setContent("우렁이와 두부가 들어간 된장찌개");
        testFood = foodRepository.save(testFood);
    }

    @Test
    void getAllMenus_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].menuId").value("20250825_A_1"))
                .andExpect(jsonPath("$[0].kind").value("A"))
                .andExpect(jsonPath("$[0].mealType").value("한식"));
    }

    @Test
    void getMenuById_ShouldReturnMenu() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/" + testMenu.getMenuNo()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuId").value("20250825_A_1"))
                .andExpect(jsonPath("$.kind").value("A"));
    }

    @Test
    void getMenuByMenuId_ShouldReturnMenu() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/id/20250825_A_1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuId").value("20250825_A_1"));
    }

    @Test
    void getMenusByCafeNo_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/cafe/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cafeNo").value(1));
    }

    @Test
    void getMenusByMealType_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/meal-type/한식"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mealType").value("한식"));
    }

    @Test
    void getSignatureMenus_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/signature"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].isSignature").value(true));
    }

    @Test
    void getMenusByDate_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/date/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].menuDate").value("2025-08-25"));
    }

    @Test
    void getWeeklyMenus_ShouldReturnMenus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/weekly/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.2025-08-25").exists());
    }

    @Test
    void getMenusByAllergy_ShouldReturnFoods() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/allergy/대두"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].allergyInfo").value(containsString("대두")));
    }

    @Test
    void getAllergyFreeMenus_ShouldReturnFoods() throws Exception {
        // Given - 알러지가 없는 음식 데이터 추가
        Food allergyFreeFood = new Food();
        allergyFreeFood.setMenuId("20250825_A_2");
        allergyFreeFood.setMenuName("흑미밥");
        allergyFreeFood.setKcal(300L);
        allergyFreeFood.setAllergy(0L);
        allergyFreeFood.setAllergyInfo("알러지 성분 없음");
        allergyFreeFood.setCategory("한식");
        allergyFreeFood.setContent("영양가 높은 흑미밥");
        foodRepository.save(allergyFreeFood);

        // When & Then
        mockMvc.perform(get("/api/menus/allergy-free"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].allergyInfo").value("알러지 성분 없음"));
    }

    @Test
    void getMenuPricesByDate_ShouldReturnPrices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/prices/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].kind").value("A"))
                .andExpect(jsonPath("$[0].price").value(6000));
    }

    @Test
    void getMenuPricesByKind_ShouldReturnPrices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/prices/kind/A/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].kind").value("A"));
    }

    @Test
    void getMenuPricesByMealType_ShouldReturnPrices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/prices/meal-type/한식/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mealType").value("한식"));
    }

    @Test
    void getMenusWithPrices_ShouldReturnMenusAndPrices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/with-prices/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menus").exists())
                .andExpect(jsonPath("$.prices").exists());
    }

    @Test
    void getMenusByDate_WithInvalidDate_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/date/invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeeklyMenus_WithInvalidDate_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menus/weekly/invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
