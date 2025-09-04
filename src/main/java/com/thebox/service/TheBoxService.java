package com.thebox.service;

import com.thebox.model.Channel;
import com.thebox.model.Country;

import java.util.List;
import java.util.Map;

public interface TheBoxService {
    List<Country> getAvailableCountries();

    List<Channel> getChannelsByCountry(String countryCode);

    List<String> getCategoriesByCountry(String countryCode);

    List<Channel> getChannelsByCategory(String countryCode, String category);

    List<Channel> searchChannels(String query, String countryCode);

    String getChannelStreamUrl(Long channelId);

    Channel getChannelById(Long channelId);

    void createRealWorkingChannels();

    Map<String, Object> checkChannelHealth(Long channelId);

//    void refreshDDChannelsWithWorkingStreams();

    String validateHLSStream(String streamUrl);

    void fetchAndUpdateChannels();

    void fetchChannelsFromGlobalStreams();

    List<Channel> getChannelsFromGlobalStreams(String countryCode);

    void createChannelsFromGlobalStreams(String... countryCodes);

    void fetchComprehensiveGlobalData();

    List<Channel> getAllChannels();

    void updateChannelLogo(String channelId, String logoUrl);
} 