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
        // 데이터 초기화 로직 비활성화
        logger.info("데이터 초기화 로직이 비활성화되었습니다.");
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
