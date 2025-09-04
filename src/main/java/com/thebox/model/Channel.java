package com.thebox.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
public class Channel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "channel_id", unique = true, nullable = false)
    private String channelId; // Unique ID from iptv-org/channels.json
    
    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank
    @Column(name = "stream_url", nullable = false, length = 1000)
    private String streamUrl;
    
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "language")
    private String language;
    
    @NotNull
    @Column(name = "country_code", nullable = false)
    private String countryCode;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "epg_id")
    private String epgId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Channel() {}
    
    public Channel(String channelId, String name, String streamUrl, String countryCode) {
        this.channelId = channelId;
        this.name = name;
        this.streamUrl = streamUrl;
        this.countryCode = countryCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Channel(String channelId, String name, String streamUrl, String logoUrl, String category, String language, String countryCode, String epgId) {
        this.channelId = channelId;
        this.name = name;
        this.streamUrl = streamUrl;
        this.logoUrl = logoUrl;
        this.category = category;
        this.language = language;
        this.countryCode = countryCode;
        this.epgId = epgId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStreamUrl() {
        return streamUrl;
    }
    
    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getEpgId() {
        return epgId;
    }
    
    public void setEpgId(String epgId) {
        this.epgId = epgId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 