package com.bapsim.repository;

import com.bapsim.entity.MenuPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuPriceRepository extends JpaRepository<MenuPrice, Long> {
    
    // 특정 종류의 현재 유효한 가격 조회 (mealType 무관)
    @Query("SELECT mp FROM MenuPrice mp WHERE mp.kind = :kind " +
           "AND mp.isActive = true " +
           "AND (mp.effectiveDate <= :date) " +
           "AND (mp.expiryDate IS NULL OR mp.expiryDate >= :date) " +
           "ORDER BY mp.effectiveDate DESC")
    Optional<MenuPrice> findCurrentPrice(@Param("kind") String kind, 
                                       @Param("date") LocalDate date);
    
    // 특정 날짜의 모든 유효한 가격 조회
    @Query("SELECT mp FROM MenuPrice mp WHERE mp.isActive = true " +
           "AND (mp.effectiveDate <= :date) " +
           "AND (mp.expiryDate IS NULL OR mp.expiryDate >= :date) " +
           "ORDER BY mp.kind, mp.mealType")
    List<MenuPrice> findAllCurrentPrices(@Param("date") LocalDate date);
    
    // 특정 종류의 현재 유효한 가격들 조회
    @Query("SELECT mp FROM MenuPrice mp WHERE mp.kind = :kind AND mp.isActive = true " +
           "AND (mp.effectiveDate <= :date) " +
           "AND (mp.expiryDate IS NULL OR mp.expiryDate >= :date) " +
           "ORDER BY mp.mealType")
    List<MenuPrice> findCurrentPricesByKind(@Param("kind") String kind, 
                                           @Param("date") LocalDate date);
    
    // 특정 식사 타입의 현재 유효한 가격들 조회
    @Query("SELECT mp FROM MenuPrice mp WHERE mp.mealType = :mealType AND mp.isActive = true " +
           "AND (mp.effectiveDate <= :date) " +
           "AND (mp.expiryDate IS NULL OR mp.expiryDate >= :date) " +
           "ORDER BY mp.kind")
    List<MenuPrice> findCurrentPricesByMealType(@Param("mealType") String mealType, 
                                               @Param("date") LocalDate date);
    
    // 특정 메뉴 ID로 가격 정보 조회
    Optional<MenuPrice> findByMenu_MenuNoAndIsActiveTrue(Long menuNo);
    
}
