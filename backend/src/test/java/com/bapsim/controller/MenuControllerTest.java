package com.bapsim.controller;

import com.bapsim.dto.MenuDTO;
import com.bapsim.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    private MenuDTO testMenuDTO;

    @BeforeEach
    void setUp() {
        // 테스트용 DTO 객체 생성
        testMenuDTO = new MenuDTO(
            1L,                // menuNo
            "우렁된장찌개",      // menuName
            6000L,             // price
            "한식",              // category
            450L,              // kcal
            "/path/to/photo.jpg", // photoPath
            true,              // isSignature
            false,             // soldOut
            "점심",              // mealType
            LocalDate.of(2025, 8, 25) // menuDate
        );
    }

    @Test
    void getMenusByDate_ShouldReturnMenus() throws Exception {
        // Given
        when(menuService.getMenusByDate(any(LocalDate.class))).thenReturn(Collections.singletonList(testMenuDTO));

        // When & Then
        mockMvc.perform(get("/api/menus/date/2025-08-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].menuNo").value(1L))
                .andExpect(jsonPath("$[0].menuName").value("우렁된장찌개"))
                .andExpect(jsonPath("$[0].price").value(6000L));
    }

    @Test
    void getMenuById_ShouldReturnMenu() throws Exception {
        // Given
        when(menuService.getMenuById(anyLong())).thenReturn(testMenuDTO);

        // When & Then
        mockMvc.perform(get("/api/menus/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.menuNo").value(1L))
                .andExpect(jsonPath("$.menuName").value("우렁된장찌개"));
    }

    @Test
    void getMenuById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(menuService.getMenuById(anyLong())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/menus/999"))
                .andExpect(status().isNotFound());
    }
}
