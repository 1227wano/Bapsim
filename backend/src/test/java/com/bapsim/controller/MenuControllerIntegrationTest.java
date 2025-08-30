package com.bapsim.controller;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.entity.MenuPrice;
import com.bapsim.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuRepository menuRepository;

    private Menus testMenu;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성 및 저장
        createTestData();
    }

    private void createTestData() {
        // 1. Menus 객체 생성
        testMenu = new Menus();
        testMenu.setKind("A");
        testMenu.setMealType("한식");
        testMenu.setIsSignature(true);
        testMenu.setSoldOut(false);
        testMenu.setCafeNo(1L);
        testMenu.setMenuDate(LocalDate.of(2025, 8, 25));
        testMenu.setCreatedId("test");
        testMenu.setCreatedAt(LocalDateTime.now());
        testMenu.setUpdatedId("test");
        testMenu.setUpdatedAt(LocalDateTime.now());

        // 2. Food 객체 생성
        Food testFood = new Food();
        testFood.setMenuName("우렁된장찌개");
        testFood.setKcal(450L);
        testFood.setAllergy(2L);
        testFood.setCategory("한식");
        testFood.setContent("우렁이와 두부가 들어간 된장찌개");
        testFood.setAllergyInfo("대두(된장, 두부), 갑각류(우렁이)");

        // 3. MenuPrice 객체 생성
        MenuPrice testMenuPrice = new MenuPrice();
        testMenuPrice.setPrice(6000L);
        testMenuPrice.setEffectiveDate(LocalDate.now());
        testMenuPrice.setIsActive(true);
        testMenuPrice.setKind("A");
        testMenuPrice.setMealType("한식");
        testMenuPrice.setCreatedId("test");
        testMenuPrice.setCreatedAt(LocalDateTime.now());
        testMenuPrice.setUpdatedId("test");
        testMenuPrice.setUpdatedAt(LocalDateTime.now());

        // 4. 연관관계 설정
        testMenu.setFoods(new ArrayList<>());
        testMenu.getFoods().add(testFood);
        testFood.setMenu(testMenu);

        testMenu.setMenuPrice(testMenuPrice);
        testMenuPrice.setMenu(testMenu);

        // 5. Menus 저장 (Food, MenuPrice는 CascadeType.ALL로 자동 저장)
        testMenu = menuRepository.save(testMenu);
    }

    @Test
    void getMenusByDate_ShouldReturnMenus() throws Exception {
        mockMvc.perform(get("/api/menus/date/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].menuNo").value(testMenu.getMenuNo()))
                .andExpect(jsonPath("$[0].menuName").value("우렁된장찌개"))
                .andExpect(jsonPath("$[0].menuPrice.price").value(6000));
    }

    @Test
    void getMenuById_ShouldReturnMenu() throws Exception {
        mockMvc.perform(get("/api/menus/" + testMenu.getMenuNo()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuNo").value(testMenu.getMenuNo()))
                .andExpect(jsonPath("$.menuName").value("우렁된장찌개"));
    }
}