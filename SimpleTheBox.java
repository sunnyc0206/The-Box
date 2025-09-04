import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class SimpleTheBox {
    
    private static final String INDIA_IPTV_URL = "https://raw.githubusercontent.com/iptv-org/iptv/master/countries/in.m3u";
    private static final String USA_IPTV_URL = "https://raw.githubusercontent.com/iptv-org/iptv/master/countries/us.m3u";
    
    private static List<Channel> channels = new ArrayList<>();
    private static Map<String, List<Channel>> channelsByCountry = new HashMap<>();
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TheBox - Simple IPTV Backend");
        System.out.println("========================================");
        System.out.println();
        
        try {
            // Start HTTP server
            startServer();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("TheBox Backend started on port 8080");
        System.out.println("Access the API at: http://localhost:8080");
        System.out.println();
        System.out.println("Available endpoints:");
        System.out.println("- GET /api/iptv/countries");
        System.out.println("- GET /api/iptv/countries/{countryCode}/channels");
        System.out.println("- GET /api/iptv/search?query={query}");
        System.out.println("- POST /api/iptv/refresh");
        System.out.println();
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println();
        
        // Load initial channels
        refreshChannels();
        
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            } catch (Exception e) {
                System.err.println("Error handling request: " + e.getMessage());
            }
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];
            
            System.out.println("Request: " + method + " " + path);
            
            // Parse query parameters
            String query = "";
            if (path.contains("?")) {
                String[] pathParts = path.split("\\?", 2);
                path = pathParts[0];
                query = pathParts[1];
            }
            
            String response = "";
            String contentType = "application/json";
            
            if (path.equals("/api/iptv/countries")) {
                response = getCountriesResponse();
            } else if (path.matches("/api/iptv/countries/[A-Z]{2}/channels")) {
                String countryCode = path.substring(path.lastIndexOf("/") + 1);
                response = getChannelsByCountryResponse(countryCode);
            } else if (path.equals("/api/iptv/search")) {
                response = searchChannelsResponse(query);
            } else if (path.equals("/api/iptv/refresh")) {
                response = refreshChannelsResponse();
            } else {
                response = "{\"error\": \"Endpoint not found\"}";
            }
            
            // Send response
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Content-Length: " + response.length());
            out.println();
            out.println(response);
            
            clientSocket.close();
            
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    private static String getCountriesResponse() {
        return "[{\"id\":1,\"name\":\"India\",\"code\":\"IN\",\"flagUrl\":\"https://flagcdn.com/w40/in.png\",\"isActive\":true},{\"id\":2,\"name\":\"United States\",\"code\":\"US\",\"flagUrl\":\"https://flagcdn.com/w40/us.png\",\"isActive\":true}]";
    }
    
    private static String getChannelsByCountryResponse(String countryCode) {
        List<Channel> countryChannels = channelsByCountry.get(countryCode);
        if (countryChannels == null) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < countryChannels.size(); i++) {
            Channel channel = countryChannels.get(i);
            if (i > 0) json.append(",");
            json.append(channel.toJson());
        }
        json.append("]");
        return json.toString();
    }
    
    private static String searchChannelsResponse(String query) {
        if (query == null || query.isEmpty()) {
            return "[]";
        }
        
        List<Channel> results = new ArrayList<>();
        String searchQuery = query.toLowerCase();
        
        for (Channel channel : channels) {
            if (channel.name.toLowerCase().contains(searchQuery)) {
                results.add(channel);
            }
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < results.size(); i++) {
            Channel channel = results.get(i);
            if (i > 0) json.append(",");
            json.append(channel.toJson());
        }
        json.append("]");
        return json.toString();
    }
    
    private static String refreshChannelsResponse() {
        try {
            refreshChannels();
            return "{\"message\": \"Channels refreshed successfully\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to refresh channels: " + e.getMessage() + "\"}";
        }
    }
    
    private static void refreshChannels() throws Exception {
        System.out.println("Refreshing channels from IPTV sources...");
        
        channels.clear();
        channelsByCountry.clear();
        
        // Fetch India channels
        fetchChannelsFromSource(INDIA_IPTV_URL, "IN");
        
        // Fetch USA channels
        fetchChannelsFromSource(USA_IPTV_URL, "US");
        
        System.out.println("Total channels loaded: " + channels.size());
        System.out.println("Channels by country:");
        for (Map.Entry<String, List<Channel>> entry : channelsByCountry.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size() + " channels");
        }
        System.out.println();
    }
    
    private static void fetchChannelsFromSource(String url, String countryCode) throws Exception {
        System.out.println("Fetching channels from: " + url);
        
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            String currentName = null;
            String currentLogo = null;
            String currentGroup = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("#EXTINF:")) {
                    // Parse channel info
                    Map<String, String> channelInfo = parseExtInfLine(line);
                    currentName = channelInfo.get("name");
                    currentLogo = channelInfo.get("logo");
                    currentGroup = channelInfo.get("group");
                } else if (line.startsWith("http") && currentName != null) {
                    // This is a stream URL, create channel
                    Channel channel = new Channel(currentName, line, countryCode, currentLogo, currentGroup);
                    channels.add(channel);
                    
                    // Add to country map
                    channelsByCountry.computeIfAbsent(countryCode, k -> new ArrayList<>()).add(channel);
                    
                    // Reset for next channel
                    currentName = null;
                    currentLogo = null;
                    currentGroup = null;
                }
            }
        }
        
        connection.disconnect();
    }
    
    private static Map<String, String> parseExtInfLine(String extInfLine) {
        Map<String, String> info = new HashMap<>();
        
        // Extract channel name
        Pattern namePattern = Pattern.compile(",(.+)$");
        Matcher nameMatcher = namePattern.matcher(extInfLine);
        if (nameMatcher.find()) {
            info.put("name", nameMatcher.group(1).trim());
        }
        
        // Extract logo URL
        Pattern logoPattern = Pattern.compile("tvg-logo=\"([^\"]+)\"");
        Matcher logoMatcher = logoPattern.matcher(extInfLine);
        if (logoMatcher.find()) {
            info.put("logo", logoMatcher.group(1));
        }
        
        // Extract group/category
        Pattern groupPattern = Pattern.compile("group-title=\"([^\"]+)\"");
        Matcher groupMatcher = groupPattern.matcher(extInfLine);
        if (groupMatcher.find()) {
            info.put("group", groupMatcher.group(1));
        }
        
        return info;
    }
    
    static class Channel {
        private static int nextId = 1;
        public int id;
        public String name;
        public String streamUrl;
        public String logoUrl;
        public String category;
        public String countryCode;
        public boolean isActive;
        
        public Channel(String name, String streamUrl, String countryCode, String logoUrl, String category) {
            this.id = nextId++;
            this.name = name;
            this.streamUrl = streamUrl;
            this.countryCode = countryCode;
            this.logoUrl = logoUrl;
            this.category = category;
            this.isActive = true;
        }
        
        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"id\":").append(id).append(",");
            json.append("\"name\":\"").append(escapeJson(name)).append("\",");
            json.append("\"streamUrl\":\"").append(escapeJson(streamUrl)).append("\",");
            if (logoUrl != null) {
                json.append("\"logoUrl\":\"").append(escapeJson(logoUrl)).append("\",");
            }
            if (category != null) {
                json.append("\"category\":\"").append(escapeJson(category)).append("\",");
            }
            json.append("\"countryCode\":\"").append(countryCode).append("\",");
            json.append("\"isActive\":").append(isActive);
            json.append("}");
            return json.toString();
        }
        
        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        }
    }
} 