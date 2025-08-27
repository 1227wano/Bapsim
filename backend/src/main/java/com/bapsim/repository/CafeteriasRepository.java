package com.bapsim.repository;

import com.bapsim.entity.Cafeterias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeteriasRepository extends JpaRepository<Cafeterias, Long> {
}
