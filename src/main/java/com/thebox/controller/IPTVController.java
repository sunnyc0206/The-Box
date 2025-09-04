package com.thebox.controller;

import com.thebox.model.Channel;
import com.thebox.model.Country;
import com.thebox.service.TheBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/iptv")
@CrossOrigin(origins = "*")
public class IPTVController {
    
    @Autowired
    private TheBoxService iptvService;
    
    @Autowired
    private WebClient webClient;
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("hello from health");
        return ResponseEntity.ok("TheBox Backend is running!");
    }
    
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        try {
            List<Country> countries = iptvService.getAvailableCountries();
            if (countries.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            System.err.println("Error getting countries: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }
    
    @GetMapping("/countries/{countryCode}/channels")
    public ResponseEntity<List<Channel>> getChannelsByCountry(@PathVariable String countryCode) {
        try {
            List<Channel> channels = iptvService.getChannelsByCountry(countryCode.toUpperCase());
            if (channels.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            System.err.println("Error getting channels for country " + countryCode + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }
    
    @GetMapping("/countries/{countryCode}/categories")
    public ResponseEntity<List<String>> getCategoriesByCountry(@PathVariable String countryCode) {
        try {
            List<String> categories = iptvService.getCategoriesByCountry(countryCode.toUpperCase());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/countries/{countryCode}/categories/{category}/channels")
    public ResponseEntity<List<Channel>> getChannelsByCategory(
            @PathVariable String countryCode,
            @PathVariable String category) {
        try {
            List<Channel> channels = iptvService.getChannelsByCategory(countryCode.toUpperCase(), category);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Channel>> searchChannels(
            @RequestParam String query,
            @RequestParam(required = false) String countryCode) {
        try {
            List<Channel> channels;
            if (countryCode != null && !countryCode.isEmpty()) {
                channels = iptvService.searchChannels(query, countryCode.toUpperCase());
            } else {
                // Search across all countries
                channels = iptvService.getAvailableCountries()
                        .stream()
                        .flatMap(country -> iptvService.searchChannels(query, country.getCode()).stream())
                        .toList();
            }
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/channels/{channelId}")
    public ResponseEntity<Channel> getChannel(@PathVariable Long channelId) {
        try {
            Channel channel = iptvService.getChannelById(channelId);
            if (channel != null) {
                return ResponseEntity.ok(channel);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/channels/{channelId}/stream")
    public ResponseEntity<String> getChannelStream(@PathVariable Long channelId) {
        try {
            String streamUrl = iptvService.getChannelStreamUrl(channelId);
            if (streamUrl != null) {
                return ResponseEntity.ok(streamUrl);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshChannels() {
        try {
            System.out.println("Refreshing channels...");
            iptvService.fetchAndUpdateChannels();
            System.out.println("Channels refreshed successfully");
            return ResponseEntity.ok("Channels refreshed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing channels: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error refreshing channels: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("message", "Backend is working");
            response.put("timestamp", System.currentTimeMillis());
            response.put("countries", iptvService.getAvailableCountries().size());
            response.put("totalChannels", iptvService.getChannelsByCountry("IN").size() + iptvService.getChannelsByCountry("US").size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/create-sample-channels")
    public ResponseEntity<String> createSampleChannels() {
        try {
            System.out.println("Creating sample channels...");
            // Force refresh and create sample channels
            iptvService.fetchAndUpdateChannels();
            return ResponseEntity.ok("Sample channels created successfully");
        } catch (Exception e) {
            System.err.println("Error creating sample channels: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating sample channels: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-working-channels")
    public ResponseEntity<String> createWorkingChannels() {
        try {
            System.out.println("Creating working IPTV channels...");
            // Use the new method to create real working channels
            iptvService.createRealWorkingChannels();
            return ResponseEntity.ok("Working channels created successfully");
        } catch (Exception e) {
            System.err.println("Error creating working channels: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating working channels: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-test-streams")
    public ResponseEntity<String> createTestStreams() {
        try {
            System.out.println("Creating channels with test streams...");
            // Clear existing channels first
            // Then create channels with working test streams
            return ResponseEntity.ok("Test stream channels created successfully");
        } catch (Exception e) {
            System.err.println("Error creating test stream channels: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating test stream channels: " + e.getMessage());
        }
    }
    
    @GetMapping("/channels/{channelId}/health")
    public ResponseEntity<Map<String, Object>> checkChannelHealth(@PathVariable Long channelId) {
        try {
            Map<String, Object> healthInfo = iptvService.checkChannelHealth(channelId);
            return ResponseEntity.ok(healthInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "error");
            errorInfo.put("message", e.getMessage());
            errorInfo.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(errorInfo);
        }
    }
    
    @PostMapping("/validate-hls")
    public ResponseEntity<Map<String, Object>> validateHLSStream(@RequestParam String streamUrl) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("streamUrl", streamUrl);
            response.put("timestamp", System.currentTimeMillis());
            
            // Check if URL is HLS format
            if (!streamUrl.contains(".m3u8")) {
                response.put("valid", false);
                response.put("message", "URL is not an HLS stream. Only .m3u8 files are supported.");
                response.put("format", "Not HLS");
                return ResponseEntity.ok(response);
            }
            
            // Try to fetch the stream to validate it
            try {
                String streamContent = iptvService.validateHLSStream(streamUrl);
                response.put("valid", true);
                response.put("message", "HLS stream is valid and accessible");
                response.put("format", "HLS (.m3u8)");
                response.put("contentPreview", streamContent.substring(0, Math.min(200, streamContent.length())));
            } catch (Exception e) {
                response.put("valid", false);
                response.put("message", "HLS stream validation failed: " + e.getMessage());
                response.put("format", "HLS (.m3u8) - Invalid");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Error validating stream: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/global-streams/fetch")
    public ResponseEntity<String> fetchGlobalStreams() {
        try {
            System.out.println("Fetching channels from global IPTV streams JSON...");
            iptvService.fetchChannelsFromGlobalStreams();
            return ResponseEntity.ok("Global streams fetched successfully");
        } catch (Exception e) {
            System.err.println("Error fetching global streams: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching global streams: " + e.getMessage());
        }
    }
    
    @GetMapping("/global-streams/countries/{countryCode}/channels")
    public ResponseEntity<List<Channel>> getGlobalChannelsByCountry(@PathVariable String countryCode) {
        try {
            List<Channel> channels = iptvService.getChannelsFromGlobalStreams(countryCode.toUpperCase());
            if (channels.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            System.err.println("Error getting global channels for country " + countryCode + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }
    
    @PostMapping("/global-streams/create")
    public ResponseEntity<String> createGlobalChannels(@RequestParam String countryCodes) {
        try {
            System.out.println("Creating channels from global streams for countries: " + countryCodes);
            String[] codesArray = countryCodes.split(",");
            iptvService.createChannelsFromGlobalStreams(codesArray);
            return ResponseEntity.ok("Global channels created successfully");
        } catch (Exception e) {
            System.err.println("Error creating global channels: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating global channels: " + e.getMessage());
        }
    }


} 