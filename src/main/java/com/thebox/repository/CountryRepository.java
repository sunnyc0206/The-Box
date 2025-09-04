package com.thebox.repository;

import com.thebox.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    
    List<Country> findByIsActiveTrue();
    
    Optional<Country> findByCode(String code);
    
    Optional<Country> findByName(String name);
    
    boolean existsByCode(String code);
} 