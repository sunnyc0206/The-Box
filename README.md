# The BOX
 
A full-stack web application to access free-to-air TV channels from around the world with real streaming capabilities and automatic stream discovery.
 
## Project Overview
- **Frontend**: React.js  for modern build tooling
- **Backend**: Spring Boot (Java) with stream monitoring and discovery
- **Video Player**: HLS.js integration for live streaming
- **Stream Monitoring**: Real-time stream health checking
- **Stream Discovery**: Automatic discovery from GitHub IPTV collections
- **Responsive Design**: Mobile and desktop friendly
 
## Features
- Channel browsing by country/region
- Search and filtering functionality
- Real-time stream monitoring
- Technical stream information
- HLS video player integration
- Channel grid/list view modes
- Stream health status indicators
- **Automatic stream discovery** from IPTV collections
- **GitHub IPTV integration** (iptv-org/iptv)
- **M3U8 playlist parsing** and validation
- **Community-driven channel discovery**
- RESTful API endpoints
- Channel data management
 
## Technical Capabilities
 
### Stream Support
- **HLS (HTTP Live Streaming)**: Full support with HLS.js
- **DASH**: Framework ready for future implementation
- **Stream Validation**: Automatic stream type detection
- **Health Monitoring**: Real-time stream accessibility checking
- **Technical Metadata**: Resolution, bitrate, codec information
 
### Stream Monitoring
- **Live Status**: ONLINE/OFFLINE/SLOW/ERROR indicators
- **Accessibility Checking**: HEAD requests to validate stream URLs
- **Batch Monitoring**: Concurrent checking of multiple streams
- **Performance Metrics**: Response times and stream quality
 
### **Stream Discovery** 🆕
- **GitHub IPTV Collections**: Integration with iptv-org/iptv repository
- **M3U8 Playlist Parsing**: Automatic parsing of IPTV playlists
- **Channel Metadata Extraction**: Name, logo, country, category, language
- **Stream Validation**: Automatic testing of discovered streams
- **Duplicate Prevention**: Smart filtering of duplicate channels
- **Batch Processing**: Concurrent discovery of multiple collections
 
### Video Player Features
- **Adaptive Quality**: HLS quality level switching
- **Error Recovery**: Automatic retry and error handling
- **Stream Information**: Real-time technical details
- **Responsive Design**: Mobile-optimized controls
 
 
## Getting Started
 
### Prerequisites
- Java 17+ (for Spring Boot)
- Node.js 18+ (for React + Vite)
- Maven (for Spring Boot)
 
### Backend Setup
1. Navigate to `backend/` directory
2. Run `mvn spring-boot:run`
3. Backend will start on `http://localhost:8080`
4. H2 Console available at `http://localhost:8080/h2-console`
5. Stream monitoring endpoints at `/api/streams/*`
6. **Stream discovery endpoints at `/api/discovery/*`** 🆕
 
### Frontend Setup
1. Navigate to `frontend/` directory
2. Run `npm install`
3. Run `npm run dev` (Vite development server) 🆕
4. Frontend will start on `http://localhost:3000`
 
 
## **GitHub IPTV Collections Integrated** 🆕
 
### **Primary Source: iptv-org/iptv**
- **Repository**: `https://github.com/iptv-org/iptv`
- **Stars**: 65,000+
- **Content**: 10,000+ channels, 100+ countries
- **Format**: M3U8 playlists, JSON metadata
- **Quality**: Professional, well-maintained
 
 
### **Discovery Process**
1. **Fetch Playlists**: Download M3U8 files from GitHub
2. **Parse Content**: Extract channel metadata and stream URLs
3. **Validate Streams**: Test accessibility of discovered streams
4. **Remove Duplicates**: Smart filtering of duplicate channels
5. **Save to Database**: Add valid channels to our system
6. **Monitor Health**: Continuous monitoring of added streams
 
## Technical Features
 
### Stream Types Supported
- **HLS (.m3u8)**: Full support with quality switching
- **DASH (.mpd)**: Framework ready
- **MPEG-TS**: Basic support
- **RTMP/RTSP**: Framework ready
 
### Stream Monitoring
- **Health Checks**: Every 5 minutes for active streams
- **Accessibility Validation**: HTTP HEAD requests
- **Performance Metrics**: Response times and error tracking
- **Batch Processing**: Concurrent monitoring of multiple streams
 
### **Stream Discovery** 🆕
- **Automatic Collection Fetching**: From GitHub IPTV repositories
- **M3U8 Playlist Parsing**: Full EXTINF metadata extraction
- **Stream Validation**: Automatic accessibility testing
- **Duplicate Prevention**: Smart channel deduplication
- **Batch Processing**: Concurrent discovery of multiple collections
- **Error Handling**: Comprehensive error reporting and recovery
 
### Video Player Capabilities
- **Adaptive Bitrate**: Automatic quality switching
- **Error Recovery**: Network and media error handling
- **Live Stream Support**: Low-latency HLS configuration
- **Mobile Optimization**: Touch-friendly controls
 
### **Build System** 🆕
- **ESBuild**: Lightning-fast bundling
- **HMR**: Hot Module Replacement for instant updates
- **Code Splitting**: Automatic chunk optimization
- **TypeScript Ready**: Full TypeScript support
 
 
## Notes
- **Legal Compliance**: Only includes free-to-air, publicly available streams
- **Stream Validation**: All streams are validated for accessibility
- **Performance**: Optimized for low-latency live streaming
- **Scalability**: Designed to handle hundreds of concurrent streams
- **Security**: CORS configured for development, production hardening needed
- **Discovery**: **Automatically discovers new streams from community sources** 🆕
- **GitHub Integration**: **Leverages iptv-org/iptv for continuous channel discovery** 🆕
- **Build Performance**: **Vite provides 10x faster development experience** 🆕
