package com.bapsim.repository;

import com.bapsim.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByAllergyInfoContaining(String allergyInfo);
    List<Food> findByAllergyInfo(String allergyInfo);
}
