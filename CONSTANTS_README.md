# Constants and Configuration Management

This project now includes a comprehensive constants and configuration management system to replace hardcoded strings throughout the application.

## Files Created

### 1. `src/main/java/com/example/constants/ApplicationConstants.java`
Contains static constants for values that never change at runtime:
- `ApplicationConstants.Eureka.*` - Eureka-specific constants
- `ApplicationConstants.Network.*` - Network-related constants  
- `ApplicationConstants.UrlBuilder.*` - Utility methods for building URLs

### 2. `src/main/resources/application.properties`
Externalized configuration properties that can be overridden in different environments:
- Eureka configuration properties
- Network configuration
- Composed properties using Spring's property placeholder syntax

### 3. `src/main/java/com/example/config/ApplicationProperties.java`
Spring Boot configuration properties class providing type-safe access to configuration values:
- Binds to properties with `app.*` prefix
- Provides nested configuration structure
- Includes default values

### 4. Updated `application.yml`
Modified to use property placeholders instead of hardcoded values:
- Uses `${property.name:defaultValue}` syntax
- Maintains backward compatibility with default values

## Usage Examples

### Using Static Constants
```java
// For values that never change
String eurekaUrl = ApplicationConstants.Eureka.DEFAULT_ZONE_URL;
String hostname = ApplicationConstants.Network.LOCALHOST;
```

### Using Configuration Properties
```java
@Autowired
private ApplicationProperties applicationProperties;

// For runtime-configurable values
String configuredUrl = applicationProperties.getConstants().getEureka().getDefaultZoneUrl();
String hostname = applicationProperties.getNetwork().getHostname();
```

### Building Dynamic URLs
```java
// Using utility methods
String customUrl = ApplicationConstants.UrlBuilder.buildEurekaUrl("prod-host", 8761, "/eureka/");
String defaultUrl = ApplicationConstants.UrlBuilder.getDefaultEurekaUrl();
```

## Configuration Override Examples

### Development Environment
```properties
# application-dev.properties
app.network.hostname=dev-eureka-server
app.network.eureka.port=8761
```

### Production Environment
```properties
# application-prod.properties
app.network.hostname=prod-eureka-cluster
app.network.eureka.port=8761
app.network.protocol=https://
```

### Docker Environment
```bash
# Environment variables
export APP_NETWORK_HOSTNAME=eureka-service
export APP_NETWORK_EUREKA_PORT=8761
```

## Benefits

1. **Maintainability**: All hardcoded strings are centralized
2. **Configurability**: Values can be overridden per environment
3. **Type Safety**: Configuration properties provide compile-time checking
4. **Documentation**: Clear structure shows what can be configured
5. **Testing**: Easy to mock or override values in tests
6. **Consistency**: Reusable constants prevent typos and inconsistencies

## Migration Strategy

1. Replace hardcoded strings with appropriate constants from `ApplicationConstants`
2. Use `ApplicationProperties` for values that need to be configurable
3. Update configuration files to use the new property structure
4. Test with different environments to ensure configuration overrides work correctly

## Best Practices

- Use static constants for values that truly never change
- Use configuration properties for environment-specific values
- Group related constants logically
- Provide meaningful default values
- Document configuration options
- Use composition in properties files to avoid duplication