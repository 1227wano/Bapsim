package com.bapsim.repository;

import com.bapsim.entity.Menus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menus, Long> {

    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria WHERE m.menuNo = :menuNo AND m.cafeNo = :cafeNo")
    Optional<Menus> findByMenuNoAndCafeNo(@Param("menuNo") Long menuNo, @Param("cafeNo") Long cafeNo);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.cafeNo = :cafeNo")
    List<Menus> findByCafeNo(@Param("cafeNo") Long cafeNo);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.resNo = :resNo")
    List<Menus> findByResNo(@Param("resNo") Long resNo);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.mealType = :mealType")
    List<Menus> findByMealType(@Param("mealType") String mealType);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.isSignature = true")
    List<Menus> findByIsSignatureTrue();
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.menuDate = :menuDate")
    List<Menus> findByMenuDate(@Param("menuDate") LocalDate menuDate);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant WHERE m.menuDate BETWEEN :startDate AND :endDate")
    List<Menus> findByMenuDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT m FROM Menus m LEFT JOIN FETCH m.food LEFT JOIN FETCH m.cafeteria LEFT JOIN FETCH m.restaurant")
    List<Menus> findAllWithFood();
}