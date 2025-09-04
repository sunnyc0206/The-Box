package com.thebox.repository;

import com.thebox.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
    List<Channel> findByCountryCode(String countryCode);
    
    List<Channel> findByCountryCodeAndIsActiveTrue(String countryCode);
    
    List<Channel> findByCategory(String category);
    
    List<Channel> findByLanguageContaining(String language);
    
    @Query("SELECT c FROM Channel c WHERE c.countryCode = :countryCode AND c.isActive = true ORDER BY c.name")
    List<Channel> findActiveChannelsByCountry(@Param("countryCode") String countryCode);
    
    @Query("SELECT DISTINCT c.category FROM Channel c WHERE c.countryCode = :countryCode AND c.isActive = true")
    List<String> findCategoriesByCountry(@Param("countryCode") String countryCode);
    
    Optional<Channel> findByNameAndCountryCode(String name, String countryCode);
    
    boolean existsByStreamUrl(String streamUrl);
    
    List<Channel> findByNameContainingIgnoreCase(String name);
    
    Optional<Channel> findByChannelId(String channelId);
} 