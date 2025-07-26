package com.example.service;

import com.example.config.ApplicationProperties;
import com.example.constants.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example service demonstrating how to use constants and configuration properties
 */
@Service
public class ExampleService {

    private final ApplicationProperties applicationProperties;

    @Autowired
    public ExampleService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Example method using static constants
     */
    public String getEurekaUrlFromConstants() {
        // Using static constants - good for values that never change
        return ApplicationConstants.Eureka.DEFAULT_ZONE_URL;
    }

    /**
     * Example method using URL builder utility
     */
    public String buildCustomEurekaUrl(String host, int port) {
        // Using utility methods for dynamic URL building
        return ApplicationConstants.UrlBuilder.buildEurekaUrl(
            host, 
            port, 
            ApplicationConstants.Network.EUREKA_PATH
        );
    }

    /**
     * Example method using configuration properties
     */
    public String getEurekaUrlFromProperties() {
        // Using configuration properties - good for values that can be changed via configuration
        return applicationProperties.getConstants().getEureka().getDefaultZoneUrl();
    }

    /**
     * Example method building URL from individual property components
     */
    public String buildEurekaUrlFromProperties() {
        ApplicationProperties.Network network = applicationProperties.getNetwork();
        return network.getProtocol() + 
               network.getHostname() + ":" + 
               network.getEureka().getPort() + 
               network.getEureka().getPath();
    }

    /**
     * Example method showing different approaches for different use cases
     */
    public void demonstrateUsage() {
        // For compile-time constants that never change
        String staticUrl = ApplicationConstants.Eureka.DEFAULT_ZONE_URL;
        
        // For runtime configuration that can be overridden
        String configUrl = applicationProperties.getConstants().getEureka().getDefaultZoneUrl();
        
        // For dynamic URL building
        String dynamicUrl = ApplicationConstants.UrlBuilder.buildEurekaUrl(
            "custom-host", 
            9999, 
            "/custom-path/"
        );
        
        System.out.println("Static URL: " + staticUrl);
        System.out.println("Config URL: " + configUrl);
        System.out.println("Dynamic URL: " + dynamicUrl);
    }
}