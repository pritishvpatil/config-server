package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class to bind application properties
 * and provide type-safe access to configuration values
 */
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Network network = new Network();
    private Constants constants = new Constants();

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Constants getConstants() {
        return constants;
    }

    public void setConstants(Constants constants) {
        this.constants = constants;
    }

    public static class Network {
        private String hostname = "localhost";
        private String protocol = "http://";
        private Eureka eureka = new Eureka();

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public Eureka getEureka() {
            return eureka;
        }

        public void setEureka(Eureka eureka) {
            this.eureka = eureka;
        }

        public static class Eureka {
            private int port = 8761;
            private String path = "/eureka/";

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }
        }
    }

    public static class Constants {
        private Eureka eureka = new Eureka();

        public Eureka getEureka() {
            return eureka;
        }

        public void setEureka(Eureka eureka) {
            this.eureka = eureka;
        }

        public static class Eureka {
            private String defaultZoneUrl = "http://localhost:8761/eureka/";

            public String getDefaultZoneUrl() {
                return defaultZoneUrl;
            }

            public void setDefaultZoneUrl(String defaultZoneUrl) {
                this.defaultZoneUrl = defaultZoneUrl;
            }
        }
    }
}