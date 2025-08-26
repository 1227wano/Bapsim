package com.bapsim.config;

import com.bapsim.service.MenuDataService;
import com.bapsim.service.MenuPriceService;
import com.bapsim.service.BasicDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // UserController의 CommandLineRunner 이후에 실행
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private MenuDataService menuDataService;
    
    @Autowired
    private MenuPriceService menuPriceService;
    
    @Autowired
    private BasicDataService basicDataService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("데이터 초기화 시작");
        
        try {
            // 1. 기본 데이터 초기화 (대학교, 사용자, 카페테리아, 레스토랑)
            basicDataService.initializeBasicData();
            
            // 2. 카페테리아 메뉴 데이터 로드
            loadMenuData("data/menus/cafeteria_menus.json");
            
            // 3. 레스토랑 메뉴 데이터 로드
            loadMenuData("data/menus/restaurant_menus.json");
            
            // 4. 메뉴 가격 정보 초기화
            menuPriceService.initializeDefaultPrices();
            
            logger.info("데이터 초기화 완료");
            
        } catch (Exception e) {
            logger.warn("데이터 초기화 중 오류 발생: {}", e.getMessage());
            // 초기화 실패해도 애플리케이션은 계속 실행
        }
    }
    
    private void loadMenuData(String jsonFilePath) {
        try {
            menuDataService.loadMenuDataFromJson(jsonFilePath);
            logger.info("메뉴 데이터 로드 완료: {}", jsonFilePath);
        } catch (Exception e) {
            logger.warn("메뉴 데이터 로드 실패: {} - {}", jsonFilePath, e.getMessage());
        }
    }
}
