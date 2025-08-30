package com.bapsim.service;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuDataServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuDataService menuDataService;

    private Map<String, Object> sampleMenuData;

    @BeforeEach
    void setUp() {
        // 테스트용 메뉴 데이터 생성 (JSON 구조와 유사하게)
        sampleMenuData = Map.of(
            "kind", "A",
            "mealType", "한식",
            "isSignature", true,
            "soldOut", false,
            "cafeNo", 1L,
            "menuDate", "2025-08-25",
            "food", Map.of(
                "menuName", "우렁된장찌개",
                "kcal", 450L,
                "category", "한식"
            )
        );
    }

    @Test
    void saveMenuData_ShouldCreateAndLinkMenuAndFoodCorrectly() {
        // Given
        // menuRepository.save()가 호출될 때, 저장되는 Menus 객체를 캡처하기 위한 설정
        ArgumentCaptor<Menus> menuCaptor = ArgumentCaptor.forClass(Menus.class);
        when(menuRepository.save(menuCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        // 테스트의 핵심 메소드 호출
        menuDataService.saveMenuData(sampleMenuData);

        // Then
        // menuRepository.save가 정확히 1번 호출되었는지 확인
        verify(menuRepository).save(any(Menus.class));

        // 저장된 Menus 객체를 가져와서 검증
        Menus capturedMenu = menuCaptor.getValue();
        assertNotNull(capturedMenu);
        assertEquals("A", capturedMenu.getKind());
        assertEquals(1L, capturedMenu.getCafeNo());

        // Menus에 Food가 올바르게 연결되었는지 확인
        assertNotNull(capturedMenu.getFoods());
        assertFalse(capturedMenu.getFoods().isEmpty());
        Food capturedFood = capturedMenu.getFoods().get(0);
        assertNotNull(capturedFood);
        assertEquals("우렁된장찌개", capturedFood.getMenuName());
        assertEquals(450L, capturedFood.getKcal());

        // Food가 Menus를 다시 참조하는지 (양방향 관계) 확인
        assertNotNull(capturedFood.getMenu());
        assertEquals(capturedMenu, capturedFood.getMenu());
    }

    @Test
    void loadMenuDataFromJson_WithInvalidPath_ShouldThrowException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            menuDataService.loadMenuDataFromJson("invalid/path.json");
        });
    }
}
