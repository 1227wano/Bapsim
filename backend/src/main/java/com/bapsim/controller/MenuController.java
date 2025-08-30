package com.bapsim.controller;

import com.bapsim.entity.Food;
import com.bapsim.entity.Menus;
import com.bapsim.entity.MenuPrice;
import com.bapsim.repository.FoodRepository;
import com.bapsim.repository.MenuRepository;
import com.bapsim.service.MenuPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private MenuPriceService menuPriceService;
    
    /**
     * 모든 메뉴 조회
     */
    @GetMapping
    public ResponseEntity<List<Menus>> getAllMenus() {
        List<Menus> menus = menuRepository.findAllWithFood();
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 특정 메뉴 조회
     */
    @GetMapping("/{menuNo}")
    public ResponseEntity<Menus> getMenuByNo(@PathVariable Long menuNo) {
        Optional<Menus> menu = menuRepository.findById(menuNo);
        return menu.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    
    
    /**
     * 카페테리아 메뉴 조회
     */
    @GetMapping("/cafeteria/{cafeNo}")
    public ResponseEntity<List<Menus>> getCafeteriaMenus(@PathVariable Long cafeNo) {
        List<Menus> menus = menuRepository.findByCafeNo(cafeNo);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 레스토랑 메뉴 조회
     */
    @GetMapping("/restaurant/{resNo}")
    public ResponseEntity<List<Menus>> getRestaurantMenus(@PathVariable Long resNo) {
        List<Menus> menus = menuRepository.findByResNo(resNo);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 음식 상세 정보 조회
     */
    @GetMapping("/{menuNo}/food")
    public ResponseEntity<List<Food>> getFoodInfo(@PathVariable Long menuNo) {
        Optional<Menus> menuOptional = menuRepository.findById(menuNo);
        if (menuOptional.isPresent()) {
            List<Food> foods = menuOptional.get().getFoods();
            if (foods != null && !foods.isEmpty()) {
                return ResponseEntity.ok(foods);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 메뉴 타입별 조회
     */
    @GetMapping("/type/{mealType}")
    public ResponseEntity<List<Menus>> getMenusByMealType(@PathVariable String mealType) {
        List<Menus> menus = menuRepository.findByMealType(mealType);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 시그니처 메뉴 조회
     */
    @GetMapping("/signature")
    public ResponseEntity<List<Menus>> getSignatureMenus() {
        List<Menus> menus = menuRepository.findByIsSignatureTrue();
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 주간 식단 조회 (날짜 기준)
     */
    @GetMapping("/weekly/{startDate}")
    public ResponseEntity<Map<String, List<Menus>>> getWeeklyMenus(@PathVariable String startDate) {
        try {
            // 시작 날짜를 LocalDate로 파싱
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = start.plusDays(6); // 7일간
            
            // 날짜 범위 내의 메뉴들을 조회
            List<Menus> weeklyMenus = menuRepository.findByMenuDateBetween(start, end);
            
            // 날짜별로 그룹화
            Map<String, List<Menus>> weeklyMenuMap = weeklyMenus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getMenuDate().toString()));
            
            return ResponseEntity.ok(weeklyMenuMap);
            
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 날짜의 메뉴 조회
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Menus>> getMenusByDate(@PathVariable String date) {
        try {
            LocalDate menuDate = LocalDate.parse(date);
            List<Menus> menus = menuRepository.findByMenuDate(menuDate);
            return ResponseEntity.ok(menus);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 알러지 정보가 포함된 메뉴 조회
     */
    @GetMapping("/allergy/{allergyType}")
    public ResponseEntity<List<Food>> getMenusByAllergy(@PathVariable String allergyType) {
        List<Food> foods = foodRepository.findByAllergyInfoContaining(allergyType);
        return ResponseEntity.ok(foods);
    }
    
    /**
     * 알러지가 없는 메뉴 조회
     */
    @GetMapping("/allergy-free")
    public ResponseEntity<List<Food>> getAllergyFreeMenus() {
        List<Food> foods = foodRepository.findByAllergyInfo("알러지 성분 없음");
        return ResponseEntity.ok(foods);
    }
    
    /**
     * 특정 날짜의 메뉴 가격 조회
     */
    @GetMapping("/prices/{date}")
    public ResponseEntity<List<MenuPrice>> getMenuPricesByDate(@PathVariable String date) {
        try {
            LocalDate menuDate = LocalDate.parse(date);
            List<MenuPrice> prices = menuPriceService.getAllCurrentPrices(menuDate);
            return ResponseEntity.ok(prices);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 종류의 메뉴 가격 조회
     */
    @GetMapping("/prices/kind/{kind}/{date}")
    public ResponseEntity<List<MenuPrice>> getMenuPricesByKind(@PathVariable String kind, @PathVariable String date) {
        try {
            LocalDate menuDate = LocalDate.parse(date);
            List<MenuPrice> prices = menuPriceService.getCurrentPricesByKind(kind, menuDate);
            return ResponseEntity.ok(prices);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 식사 타입의 메뉴 가격 조회
     */
    @GetMapping("/prices/meal-type/{mealType}/{date}")
    public ResponseEntity<List<MenuPrice>> getMenuPricesByMealType(@PathVariable String mealType, @PathVariable String date) {
        try {
            LocalDate menuDate = LocalDate.parse(date);
            List<MenuPrice> prices = menuPriceService.getCurrentPricesByMealType(mealType, menuDate);
            return ResponseEntity.ok(prices);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 메뉴와 가격 정보를 함께 조회
     */
    @GetMapping("/with-prices/{date}")
    public ResponseEntity<Map<String, Object>> getMenusWithPrices(@PathVariable String date) {
        try {
            LocalDate menuDate = LocalDate.parse(date);
            
            // 해당 날짜의 메뉴들 조회
            List<Menus> menus = menuRepository.findByMenuDate(menuDate);
            
            // 해당 날짜의 가격 정보들 조회
            List<MenuPrice> prices = menuPriceService.getAllCurrentPrices(menuDate);
            
            // 가격 정보를 Map으로 변환 (kind + mealType을 키로 사용)
            Map<String, MenuPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(
                    price -> price.getKind() + "_" + price.getMealType(),
                    price -> price
                ));
            
            // 메뉴에 가격 정보 추가
            List<Map<String, Object>> menusWithPrices = menus.stream()
                .map(menu -> {
                    Map<String, Object> menuWithPrice = new HashMap<>();
                    menuWithPrice.put("menu", menu);
                    
                    String priceKey = menu.getKind() + "_" + menu.getMealType();
                    MenuPrice price = priceMap.get(priceKey);
                    if (price != null) {
                        menuWithPrice.put("price", price.getPrice());
                        menuWithPrice.put("priceDescription", price.getDescription());
                    }
                    
                    return menuWithPrice;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("menus", menusWithPrices);
            result.put("prices", prices);
            
            return ResponseEntity.ok(result);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
