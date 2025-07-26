package com.example.constants;

/**
 * Application-wide constants to replace hardcoded strings
 */
public final class ApplicationConstants {
    
    // Private constructor to prevent instantiation
    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Eureka Configuration Constants
    public static final class Eureka {
        public static final String DEFAULT_ZONE_URL = "http://localhost:8761/eureka/";
        public static final String HOSTNAME = "localhost";
        public static final boolean REGISTER_WITH_EUREKA = true;
        public static final boolean FETCH_REGISTRY = true;
        
        private Eureka() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
    
    // Network Configuration Constants
    public static final class Network {
        public static final String LOCALHOST = "localhost";
        public static final int EUREKA_PORT = 8761;
        public static final String HTTP_PROTOCOL = "http://";
        public static final String EUREKA_PATH = "/eureka/";
        
        private Network() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
    
    // URL Builder Methods
    public static final class UrlBuilder {
        public static String buildEurekaUrl(String host, int port, String path) {
            return Network.HTTP_PROTOCOL + host + ":" + port + path;
        }
        
        public static String getDefaultEurekaUrl() {
            return buildEurekaUrl(Network.LOCALHOST, Network.EUREKA_PORT, Network.EUREKA_PATH);
        }
        
        private UrlBuilder() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}