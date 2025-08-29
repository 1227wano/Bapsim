package com.bapsim.service;

import com.bapsim.entity.MenuPrice;
import com.bapsim.repository.MenuPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MenuPriceService {
    
    private static final Logger logger = LoggerFactory.getLogger(MenuPriceService.class);
    
    @Autowired
    private MenuPriceRepository menuPriceRepository;
    
    /**
     * 특정 종류의 현재 유효한 가격 조회 (mealType 무관)
     */
    public Optional<MenuPrice> getCurrentPrice(String kind, LocalDate date) {
        return menuPriceRepository.findCurrentPrice(kind, date);
    }
    
    /**
     * 특정 날짜의 모든 유효한 가격 조회
     */
    public List<MenuPrice> getAllCurrentPrices(LocalDate date) {
        return menuPriceRepository.findAllCurrentPrices(date);
    }
    
    /**
     * 특정 종류의 현재 유효한 가격들 조회
     */
    public List<MenuPrice> getCurrentPricesByKind(String kind, LocalDate date) {
        return menuPriceRepository.findCurrentPricesByKind(kind, date);
    }
    
    /**
     * 특정 식사 타입의 현재 유효한 가격들 조회
     */
    public List<MenuPrice> getCurrentPricesByMealType(String mealType, LocalDate date) {
        return menuPriceRepository.findCurrentPricesByMealType(mealType, date);
    }
    
    /**
     * 새로운 가격 정보 생성
     */
    @Transactional
    public MenuPrice createMenuPrice(MenuPrice menuPrice) {
        String currentUser = "system";
        LocalDateTime now = LocalDateTime.now();
        
        menuPrice.setCreatedId(currentUser);
        menuPrice.setCreatedAt(now);
        menuPrice.setUpdatedId(currentUser);
        menuPrice.setUpdatedAt(now);
        menuPrice.setIsActive(true);
        
        return menuPriceRepository.save(menuPrice);
    }
    
    /**
     * 가격 정보 수정
     */
    @Transactional
    public MenuPrice updateMenuPrice(Long priceNo, MenuPrice updatedPrice) {
        Optional<MenuPrice> existingPrice = menuPriceRepository.findById(priceNo);
        if (existingPrice.isPresent()) {
            MenuPrice price = existingPrice.get();
            
            price.setPrice(updatedPrice.getPrice());
            price.setDescription(updatedPrice.getDescription());
            price.setEffectiveDate(updatedPrice.getEffectiveDate());
            price.setExpiryDate(updatedPrice.getExpiryDate());
            price.setIsActive(updatedPrice.getIsActive());
            price.setUpdatedId("system");
            price.setUpdatedAt(LocalDateTime.now());
            
            return menuPriceRepository.save(price);
        }
        return null;
    }
    
    /**
     * 가격 정보 비활성화
     */
    @Transactional
    public boolean deactivateMenuPrice(Long priceNo) {
        Optional<MenuPrice> existingPrice = menuPriceRepository.findById(priceNo);
        if (existingPrice.isPresent()) {
            MenuPrice price = existingPrice.get();
            price.setIsActive(false);
            price.setUpdatedId("system");
            price.setUpdatedAt(LocalDateTime.now());
            menuPriceRepository.save(price);
            return true;
        }
        return false;
    }
    
    /**
     * 기본 가격 정보 초기화 (시스템 시작 시)
     */
    @Transactional
    public void initializeDefaultPrices() {
        LocalDate today = LocalDate.now();
        
        // A 식단 (레스토랑 한식) - 밥, 국, 반찬
        createMenuPriceIfNotExists("A", "한식", 6000L, "밥, 국, 반찬", today);
        
        // B 식단 (레스토랑 일품) - 밥, 국, 반찬
        createMenuPriceIfNotExists("B", "일품", 7000L, "밥, 국, 반찬", today);
        
        // C 식단 (카페테리아 도시락) - 도시락
        createMenuPriceIfNotExists("C", "도시락", 6500L, "도시락", today);
        
        // D 식단 (카페테리아 브런치) - 샌드위치/샐러드
        createMenuPriceIfNotExists("D", "브런치", 5500L, "샌드위치/샐러드", today);
        
        // E 식단 (카페테리아 샐러드) - 샐러드
        createMenuPriceIfNotExists("E", "샐러드", 5000L, "샐러드", today);
        
        logger.info("기본 메뉴 가격 정보 초기화 완료");
    }
    
    private void createMenuPriceIfNotExists(String kind, String mealType, Long price, 
                                          String description, LocalDate effectiveDate) {
        Optional<MenuPrice> existingPrice = getCurrentPrice(kind, effectiveDate);
        if (existingPrice.isEmpty()) {
            MenuPrice newPrice = new MenuPrice();
            newPrice.setKind(kind);
            newPrice.setMealType(mealType);
            newPrice.setPrice(price);
            newPrice.setDescription(description);
            newPrice.setEffectiveDate(effectiveDate);
            newPrice.setExpiryDate(null); // 계속 적용
            newPrice.setIsActive(true);
            
            createMenuPrice(newPrice);
        }
    }
}
