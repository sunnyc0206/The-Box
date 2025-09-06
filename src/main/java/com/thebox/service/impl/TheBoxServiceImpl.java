package com.thebox.service.impl;

import com.thebox.model.Channel;
import com.thebox.model.Country;
import com.thebox.repository.ChannelRepository;
import com.thebox.repository.CountryRepository;
import com.thebox.service.TheBoxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TheBoxServiceImpl implements TheBoxService {

    private static final Logger logger = LoggerFactory.getLogger(TheBoxServiceImpl.class);

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private WebClient webClient;

    // Global streams JSON URL
    private static final String GLOBAL_STREAMS_URL = "https://iptv-org.github.io/api/streams.json";
    private static final String CHANNELS_API_URL = "https://iptv-org.github.io/api/channels.json";
    private static final String LOGOS_API_URL = "https://iptv-org.github.io/api/logos.json";

    private Map<String, JsonNode> globalChannelsMetadata = new HashMap<>();
    private Map<String, JsonNode> globalCountriesMetadata = new HashMap<>();
    private Map<String, String> globalLogosMetadata = new HashMap<>();

    @Override
    @CacheEvict(value = {"channels", "countries", "categories"}, allEntries = true)
    public void fetchAndUpdateChannels() {
        logger.info("Starting to fetch and update IPTV channels from global sources...");

        try {
            fetchComprehensiveGlobalData();
            fetchChannelsFromGlobalStreams();

            logger.info("Successfully fetched and updated IPTV channels from global sources");
        } catch (Exception e) {
            logger.error("Error fetching IPTV channels from global sources: {}", e.getMessage(), e);
        }
    }

    @Override
    public void fetchComprehensiveGlobalData() {
        logger.info("Fetching comprehensive global data (channels, countries, logos)...");
        try {
            // Fetch channels metadata
            String channelsJson = webClient.get().uri(CHANNELS_API_URL).retrieve().bodyToMono(String.class).timeout(java.time.Duration.ofSeconds(30)).block();
            if (channelsJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(channelsJson);
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        if (node.has("id")) {
                            globalChannelsMetadata.put(node.get("id").asText(), node);
                        }
                    }
                }
                logger.info("Fetched {} channel metadata entries.", globalChannelsMetadata.size());
            }
            ClassPathResource countriesJson = new ClassPathResource("countriesInfo.json");

            if (countriesJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(countriesJson.getInputStream());
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        if (node.has("code")) {
                            globalCountriesMetadata.put(node.get("code").asText(), node);
                            // Create country entities directly from this API
                            createCountryFromMetadata(node);
                        }
                    }
                }
            }

            // Fetch logos metadata
            String logosJson = webClient.get().uri(LOGOS_API_URL).retrieve().bodyToMono(String.class).timeout(java.time.Duration.ofSeconds(30)).block();
            if (logosJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(logosJson);
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        if (node.has("channel") && node.has("url")) {
                            // Prioritize high-quality logos if available, otherwise take the first one.
                            String channelId = node.get("channel").asText();
                            String logoUrl = node.get("url").asText();
                            if (!globalLogosMetadata.containsKey(channelId) || (node.has("width") && node.get("width").asInt() > 100)) { // Example: prefer larger logos
                                globalLogosMetadata.put(channelId, logoUrl);
                            }
                        }
                    }
                }
                logger.info("Fetched {} logo metadata entries.", globalLogosMetadata.size());
            }

        } catch (Exception e) {
            logger.error("Error fetching comprehensive global data: {}", e.getMessage(), e);
        }
    }

    private void createCountryFromMetadata(JsonNode countryNode) {
        try {
            String code = countryNode.get("code").asText();
            String name = countryNode.get("name").asText();
            String flagUrl = countryNode.has("image") ? countryNode.get("image").asText() : null;

            Optional<Country> existingCountryOpt = countryRepository.findByCode(code);
            if (existingCountryOpt.isPresent()) {
                // Update existing country if any field changed
                Country existingCountry = existingCountryOpt.get();
                boolean updated = false;
                if (!Objects.equals(existingCountry.getName(), name)) {
                    existingCountry.setName(name);
                    updated = true;
                }
                if (!Objects.equals(existingCountry.getFlagUrl(), flagUrl)) {
                    existingCountry.setFlagUrl(flagUrl);
                    updated = true;
                }
                if (updated) {
                    countryRepository.save(existingCountry);
                    logger.debug("Updated country: {} ({})", name, code);
                }
            } else {
                // Insert new country
                Country newCountry = new Country(name, code);
                newCountry.setFlagUrl(flagUrl);
                countryRepository.save(newCountry);
                logger.debug("Created country: {} ({})", name, code);
            }
        } catch (Exception e) {
            logger.error("Error creating/updating country from metadata {}: {}", countryNode, e.getMessage(), e);
        }
    }


    @Override
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Override
    public void updateChannelLogo(String channelId, String logoUrl) {
        channelRepository.findByChannelId(channelId).ifPresent(channel -> {
            channel.setLogoUrl(logoUrl);
            channelRepository.save(channel);
        });
    }

    @Override
    @Cacheable(value = "channels", key = "#countryCode")
    public List<Channel> getChannelsByCountry(String countryCode) {
        List<Channel> channels = channelRepository.findActiveChannelsByCountry(countryCode);
        if (channels.isEmpty()) {
            logger.info("No channels found for country {}, attempting to fetch and update from global sources...", countryCode);
            try {
                fetchAndUpdateChannels();
                channels = channelRepository.findActiveChannelsByCountry(countryCode);
            } catch (Exception e) {
                logger.error("Error fetching channels for country {}: {}", countryCode, e.getMessage());
            }
        }
        return channels;
    }

    @Override
    @Cacheable(value = "channels", key = "#countryCode + '_' + #category")
    public List<Channel> getChannelsByCategory(String countryCode, String category) {
        return channelRepository.findByCountryCodeAndIsActiveTrue(countryCode)
                .stream()
                .filter(channel -> category.equals(channel.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "countries")
    public List<Country> getAvailableCountries() {
        List<Country> countries = countryRepository.findByIsActiveTrue();
        if (countries.isEmpty()) {
            logger.info("No countries found, attempting to fetch and update from global sources...");
            try {
                fetchAndUpdateChannels();
                countries = countryRepository.findByIsActiveTrue();
            } catch (Exception e) {
                logger.error("Error fetching countries: {}", e.getMessage());
            }
        }
        return countries;
    }

    @Override
    @Cacheable(value = "categories", key = "#countryCode")
    public List<String> getCategoriesByCountry(String countryCode) {
        return channelRepository.findCategoriesByCountry(countryCode);
    }

    @Override
    public List<Channel> searchChannels(String query, String countryCode) {
        return channelRepository.findByCountryCodeAndIsActiveTrue(countryCode)
                .stream()
                .filter(channel -> channel.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public String getChannelStreamUrl(Long channelId) {
        Optional<Channel> channel = channelRepository.findById(channelId);
        return channel.map(Channel::getStreamUrl).orElse(null);
    }

    @Override
    public Channel getChannelById(Long channelId) {
        return channelRepository.findById(channelId).orElse(null);
    }

    @Override
    public void createRealWorkingChannels() {
        logger.info("Creating real working IPTV channels from global streams...");
        fetchAndUpdateChannels();
    }

    @Override
    public Map<String, Object> checkChannelHealth(Long channelId) {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            Optional<Channel> channelOpt = channelRepository.findById(channelId);
            if (channelOpt.isEmpty()) {
                healthInfo.put("status", "error");
                healthInfo.put("message", "Channel not found");
                return healthInfo;
            }

            Channel channel = channelOpt.get();
            healthInfo.put("channelId", channelId);
            healthInfo.put("channelName", channel.getName());
            healthInfo.put("streamUrl", channel.getStreamUrl());
            healthInfo.put("countryCode", channel.getCountryCode());
            healthInfo.put("timestamp", System.currentTimeMillis());

            // Check stream availability
            try {
                String response = webClient.get()
                        .uri(channel.getStreamUrl())
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(10))
                        .block();

                if (response != null && response.contains(".m3u8")) {
                    healthInfo.put("streamStatus", "online");
                    healthInfo.put("streamResponse", "HLS stream appears to be working");
                    healthInfo.put("streamType", "HLS (.m3u8)");
                } else if (response != null && (response.contains(".m3u") || response.contains("#EXTM3U"))) {
                    healthInfo.put("streamStatus", "warning");
                    healthInfo.put("streamResponse", "Non-HLS stream detected. Only .m3u8 streams are supported.");
                    healthInfo.put("streamType", "Playlist (.m3u) - Not Supported");
                } else {
                    healthInfo.put("streamStatus", "offline");
                    healthInfo.put("streamResponse", "Stream may be offline or invalid");
                    healthInfo.put("streamType", "Unknown");
                }
            } catch (Exception e) {
                healthInfo.put("streamStatus", "error");
                healthInfo.put("streamResponse", "Error checking stream: " + e.getMessage());
                healthInfo.put("streamType", "Error");
            }

            healthInfo.put("status", "success");

        } catch (Exception e) {
            healthInfo.put("status", "error");
            healthInfo.put("message", "Error checking channel health: " + e.getMessage());
            logger.error("Error checking channel health for ID {}: {}", channelId, e.getMessage(), e);
        }

        return healthInfo;
    }

//    @Override
//    public void refreshDDChannelsWithWorkingStreams() {
//        logger.info("Refreshing DD channels by triggering a full channel update from global streams...");
//        fetchAndUpdateChannels();
//    }

    @Override
    public String validateHLSStream(String streamUrl) {
        logger.info("Validating HLS stream: {}", streamUrl);

        if (!streamUrl.contains(".m3u8")) {
            throw new IllegalArgumentException("URL is not an HLS stream. Only .m3u8 files are supported.");
        }

        try {
            String response = webClient.get()
                        .uri(streamUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(10))
                        .block();

                if (response == null || response.trim().isEmpty()) {
                    throw new RuntimeException("HLS stream returned empty content");
                }

                // Validate that it's actually an HLS stream
                if (!response.contains("#EXTM3U") && !response.contains(".m3u8")) {
                    throw new RuntimeException("Content does not appear to be a valid HLS stream");
                }

                logger.info("HLS stream validation successful for: {}", streamUrl);
                return response;

            } catch (Exception e) {
                logger.error("HLS stream validation failed for {}: {}", streamUrl, e.getMessage());
                throw new RuntimeException("HLS stream validation failed: " + e.getMessage(), e);
            }
    }

    @Override
    public void fetchChannelsFromGlobalStreams() {
        logger.info("Fetching channels from global IPTV streams JSON...");

        try {
            String jsonContent = webClient.get()
                    .uri(GLOBAL_STREAMS_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(30))
                    .block();

            if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                logger.info("Received global streams JSON: {} characters", jsonContent.length());
                parseStreamsJson(jsonContent);
            } else {
                logger.warn("Received empty or null global streams JSON");
            }
        } catch (Exception e) {
            logger.error("Error fetching global streams JSON: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Channel> getChannelsFromGlobalStreams(String countryCode) {
        return channelRepository.findByCountryCodeAndIsActiveTrue(countryCode)
                .stream()
                .filter(channel -> channel.getStreamUrl().contains(".m3u8"))
                .collect(Collectors.toList());
    }

    @Override
    public void createChannelsFromGlobalStreams(String... countryCodes) {
        logger.info("Creating channels from global streams for countries: {}", String.join(", ", countryCodes));

        // First fetch the global streams
        fetchChannelsFromGlobalStreams();

        // Then filter and create channels for specific countries
        for (String countryCode : countryCodes) {
            List<Channel> globalChannels = getChannelsFromGlobalStreams(countryCode);
            logger.info("Found {} global channels for country {}", globalChannels.size(), countryCode);
        }
    }

    private void parseStreamsJson(String jsonContent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);

            if (rootNode.isArray()) {
                int channelCount = 0;
                int hlsChannelCount = 0;
                Set<String> countries = new HashSet<>();

                for (JsonNode streamNode : rootNode) {
                    try {
                        String streamChannelId = streamNode.has("channel") ? streamNode.get("channel").asText() : null;
                        String url = streamNode.has("url") ? streamNode.get("url").asText() : null;
                        String quality = streamNode.has("quality") ? streamNode.get("quality").asText() : null;

                        // Only process HLS streams (.m3u8) and if channel metadata is available
                        if (url != null && url.contains(".m3u8") && streamChannelId != null && globalChannelsMetadata.containsKey(streamChannelId)) {
                            JsonNode channelMetadata = globalChannelsMetadata.get(streamChannelId);
                            String channelName = channelMetadata.has("name") ? channelMetadata.get("name").asText() : streamChannelId;
                            String countryCode = channelMetadata.has("country") ? channelMetadata.get("country").asText() : "US"; // Default to US if not found
                            String category = channelMetadata.has("categories") && channelMetadata.get("categories").isArray() && channelMetadata.get("categories").size() > 0 ? channelMetadata.get("categories").get(0).asText() : "Global Stream";
                            String language = channelMetadata.has("languages") && channelMetadata.get("languages").isArray() && channelMetadata.get("languages").size() > 0 ? channelMetadata.get("languages").get(0).asText() : "en"; // Default to English
                            String logoUrl = globalLogosMetadata.getOrDefault(streamChannelId, null);

                            // Create or update channel
                            saveGlobalStreamChannel(streamChannelId, channelName, url, logoUrl, category, language, countryCode, quality);
                            countries.add(countryCode);
                            hlsChannelCount++;
                        }
                        channelCount++;
                    } catch (Exception e) {
                        logger.debug("Error parsing stream node: {}", e.getMessage());
                    }
                }

                // Countries are now created directly from countries.json in fetchComprehensiveGlobalData
                // So, no need to call createCountriesFromCodes here.

                logger.info("Parsed {} total streams, created {} HLS channels for {} countries",
                           channelCount, hlsChannelCount, countries.size());
            }
        } catch (Exception e) {
            logger.error("Error parsing global streams JSON: {}", e.getMessage(), e);
        }
    }

    private void saveGlobalStreamChannel(String channelId, String name, String url, String logoUrl, String category, String language, String countryCode, String quality) {
        try {
            // Check if channel already exists using the new channelId
            Optional<Channel> existingChannel = channelRepository.findByChannelId(channelId);

            if (existingChannel.isPresent()) {
                // Update existing channel
                Channel channel = existingChannel.get();
                channel.setName(name);
                channel.setStreamUrl(url);
                channel.setLogoUrl(logoUrl);
                channel.setCategory(category);
                channel.setLanguage(language);
                channel.setCountryCode(countryCode);
                channel.setUpdatedAt(java.time.LocalDateTime.now());
                channelRepository.save(channel);
            } else {
                // Create new channel
                Channel channel = new Channel(channelId, name, url, logoUrl, category, language, countryCode, null); // EPG ID not available from this JSON
                channelRepository.save(channel);
            }
        } catch (Exception e) {
            logger.error("Error saving global stream channel {}: {}", name, e.getMessage(), e);
        }
    }

    private String findWorkingStreamFromGlobal(String channelName) {
        // This method is now effectively deprecated as we're fetching all streams from global JSON
        return null;
    }
} 