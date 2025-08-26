package com.bapsim.controller;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.entity.MenuPrice;
import com.bapsim.repository.FoodRepository;
import com.bapsim.repository.MenuRepository;
import com.bapsim.service.MenuPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuRepository menuRepository;

    @MockBean
    private FoodRepository foodRepository;

    @MockBean
    private MenuPriceService menuPriceService;

    @Autowired
    private ObjectMapper objectMapper;

    private Menus testMenu;
    private Food testFood;
    private MenuPrice testMenuPrice;

    @BeforeEach
    void setUp() {
        // 테스트용 메뉴 데이터 생성
        testMenu = new Menus();
        testMenu.setMenuNo(1L);
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

        // 테스트용 음식 데이터 생성
        testFood = new Food();
        testFood.setMenuId("20250825_A_1");
        testFood.setMenuName("우렁된장찌개");
        testFood.setKcal(450L);
        testFood.setAllergy(2L);
        testFood.setAllergyInfo("대두(된장, 두부), 갑각류(우렁이)");
        testFood.setCategory("한식");
        testFood.setContent("우렁이와 두부가 들어간 된장찌개");

        // 테스트용 메뉴 가격 데이터 생성
        testMenuPrice = new MenuPrice();
        testMenuPrice.setPriceNo(1L);
        testMenuPrice.setKind("A");
        testMenuPrice.setMealType("한식");
        testMenuPrice.setPrice(6000L);
        testMenuPrice.setDescription("밥, 국, 반찬");
        testMenuPrice.setEffectiveDate(LocalDate.of(2025, 8, 25));
        testMenuPrice.setIsActive(true);
    }

    @Test
    void getAllMenus_ShouldReturnMenus() throws Exception {
        // Given
        when(menuRepository.findAll()).thenReturn(Arrays.asList(testMenu));

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
        // Given
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuId").value("20250825_A_1"))
                .andExpect(jsonPath("$.kind").value("A"));
    }

    @Test
    void getMenuById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/menus/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMenuByMenuId_ShouldReturnMenu() throws Exception {
        // Given
        when(menuRepository.findByMenuId("20250825_A_1")).thenReturn(testMenu);

        // When & Then
        mockMvc.perform(get("/api/menus/id/20250825_A_1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuId").value("20250825_A_1"));
    }

    @Test
    void getMenusByCafeNo_ShouldReturnMenus() throws Exception {
        // Given
        when(menuRepository.findByCafeNo(1L)).thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/cafe/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cafeNo").value(1));
    }

    @Test
    void getMenusByResNo_ShouldReturnMenus() throws Exception {
        // Given
        testMenu.setResNo(1L);
        when(menuRepository.findByResNo(1L)).thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/restaurant/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].resNo").value(1));
    }

    @Test
    void getMenusByMealType_ShouldReturnMenus() throws Exception {
        // Given
        when(menuRepository.findByMealType("한식")).thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/meal-type/한식"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mealType").value("한식"));
    }

    @Test
    void getSignatureMenus_ShouldReturnMenus() throws Exception {
        // Given
        when(menuRepository.findByIsSignatureTrue()).thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/signature"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].isSignature").value(true));
    }

    @Test
    void getWeeklyMenus_ShouldReturnMenus() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 8, 25);
        LocalDate endDate = startDate.plusDays(6);
        when(menuRepository.findByMenuDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/weekly/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.2025-08-25").exists());
    }

    @Test
    void getMenusByDate_ShouldReturnMenus() throws Exception {
        // Given
        LocalDate menuDate = LocalDate.of(2025, 8, 25);
        when(menuRepository.findByMenuDate(menuDate)).thenReturn(Arrays.asList(testMenu));

        // When & Then
        mockMvc.perform(get("/api/menus/date/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].menuDate").value("2025-08-25"));
    }

    @Test
    void getMenusByAllergy_ShouldReturnFoods() throws Exception {
        // Given
        when(foodRepository.findByAllergyInfoContaining("대두"))
                .thenReturn(Arrays.asList(testFood));

        // When & Then
        mockMvc.perform(get("/api/menus/allergy/대두"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].allergyInfo").value(containsString("대두")));
    }

    @Test
    void getAllergyFreeMenus_ShouldReturnFoods() throws Exception {
        // Given
        when(foodRepository.findByAllergyInfo("알러지 성분 없음"))
                .thenReturn(Arrays.asList(testFood));

        // When & Then
        mockMvc.perform(get("/api/menus/allergy-free"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getMenuPricesByDate_ShouldReturnPrices() throws Exception {
        // Given
        LocalDate menuDate = LocalDate.of(2025, 8, 25);
        when(menuPriceService.getAllCurrentPrices(menuDate))
                .thenReturn(Arrays.asList(testMenuPrice));

        // When & Then
        mockMvc.perform(get("/api/menus/prices/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].kind").value("A"))
                .andExpect(jsonPath("$[0].price").value(6000));
    }

    @Test
    void getMenuPricesByKind_ShouldReturnPrices() throws Exception {
        // Given
        LocalDate menuDate = LocalDate.of(2025, 8, 25);
        when(menuPriceService.getCurrentPricesByKind("A", menuDate))
                .thenReturn(Arrays.asList(testMenuPrice));

        // When & Then
        mockMvc.perform(get("/api/menus/prices/kind/A/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].kind").value("A"));
    }

    @Test
    void getMenuPricesByMealType_ShouldReturnPrices() throws Exception {
        // Given
        LocalDate menuDate = LocalDate.of(2025, 8, 25);
        when(menuPriceService.getCurrentPricesByMealType("한식", menuDate))
                .thenReturn(Arrays.asList(testMenuPrice));

        // When & Then
        mockMvc.perform(get("/api/menus/prices/meal-type/한식/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].mealType").value("한식"));
    }

    @Test
    void getMenusWithPrices_ShouldReturnMenusAndPrices() throws Exception {
        // Given
        LocalDate menuDate = LocalDate.of(2025, 8, 25);
        when(menuRepository.findByMenuDate(menuDate))
                .thenReturn(Arrays.asList(testMenu));
        when(menuPriceService.getAllCurrentPrices(menuDate))
                .thenReturn(Arrays.asList(testMenuPrice));

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
