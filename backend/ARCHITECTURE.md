# Wazai Backend Architecture

## Overview

Wazai is a multi-source activity aggregation platform designed to support various data providers including tech events (Connpass), community gatherings, medical clinics, and more. The architecture follows SOLID principles, particularly the **Open-Closed Principle** and **Strategy Pattern**, enabling easy extension without modifying existing code.

## Architecture Principles

### 1. Provider Pattern (Strategy Pattern)

All data sources implement the `ActivityProvider` interface, ensuring consistent behavior:

```java
public interface ActivityProvider {
    List<WazaiActivity> search(String keyword);
    String getProviderName();
}
```

### 2. Universal Data Format

All providers transform their specific data into `WazaiActivity`, the unified format consumed by frontend:

```java
public record WazaiActivity(
    String id,              // e.g., "connpass-123", "clinic-456"
    String title,
    String description,
    String url,
    double latitude,
    double longitude,
    LocalDateTime startTime,
    ActivityType type,      // TECH_EVENT, CLINIC, COMMUNITY, etc.
    String source           // "Connpass", "Google", "InternalDB"
)
```

### 3. Aggregator Service

`WazaiSearchService` coordinates all providers using Spring's dependency injection:

```java
@Service
public class WazaiSearchService {
    // Spring automatically injects all ActivityProvider implementations
    public WazaiSearchService(List<ActivityProvider> providers) {
        this.providers = providers;
    }
}
```

## Project Structure

```
dev.koukeneko.wazai
├── config
│   └── AppConfig.java
├── controller
│   ├── HelloController.java
│   └── SearchController.java          # Main API endpoint
├── dto
│   ├── WazaiActivity.java             # Universal format
│   └── external                        # Provider-specific DTOs
│       ├── ConnpassEvent.java
│       └── ConnpassResponse.java
└── service
    ├── ActivityProvider.java          # Core interface
    ├── WazaiSearchService.java        # Aggregator
    └── impl                            # Provider implementations
        └── ConnpassProvider.java
```

## API Endpoints

### Search Activities
```
GET /api/search?keyword=Java
```

Returns unified activity list from all providers.

### Get Available Providers
```
GET /api/search/providers
```

Returns list of registered provider names.

## Adding a New Provider

To add a new data source (e.g., Google Community, Taiwan Tech Community):

1. **Create DTO in `dto/external/`** (if needed)
2. **Implement `ActivityProvider`**:

```java
@Service
public class GoogleCommunityProvider implements ActivityProvider {

    @Override
    public List<WazaiActivity> search(String keyword) {
        // Fetch from Google API
        // Transform to WazaiActivity
        return activities;
    }

    @Override
    public String getProviderName() {
        return "Google Community";
    }
}
```

3. **No other changes required!** Spring automatically detects the new provider.

## Benefits of This Architecture

1. **Open-Closed Principle**: Add new providers without modifying existing code
2. **Single Responsibility**: Each provider handles one data source
3. **Testability**: Easy to mock individual providers
4. **Scalability**: Parallel search across providers
5. **Maintainability**: Clear separation of concerns

## Interview Talking Points

> "I initially implemented a single Connpass integration, but recognizing the need for multi-source support (Google Community, clinics, Taiwan tech events), I refactored using the **Strategy Pattern** and **Adapter Pattern**. This architecture allows seamless addition of new data providers without modifying existing code, demonstrating the **Open-Closed Principle** from SOLID design."

## Future Enhancements

- [ ] Add geocoding for address-based location extraction
- [ ] Implement caching layer for frequently searched keywords
- [ ] Add proper logging framework (SLF4J + Logback)
- [ ] Implement rate limiting for external API calls
- [ ] Add health check endpoints for each provider
