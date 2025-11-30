# How to Add a New Provider - Complete Example

This guide demonstrates how easy it is to extend Wazai with new data sources.

## Example: Adding a Medical Clinic Provider

Let's add support for searching medical clinics in Tokyo that serve Mandarin-speaking patients.

### Step 1: Create the Provider Implementation

Create `ClinicProvider.java` in `service/impl/`:

```java
package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.WazaiActivity;
import dev.koukeneko.wazai.dto.WazaiActivity.ActivityType;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClinicProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Medical Clinics";

    @Override
    public List<WazaiActivity> search(String keyword) {
        // TODO: Replace with actual database query or external API call
        return searchClinics(keyword);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private List<WazaiActivity> searchClinics(String keyword) {
        List<WazaiActivity> clinics = new ArrayList<>();

        // Mock data - replace with actual DB/API calls
        if (containsSpecialty(keyword, "internal", "内科")) {
            clinics.add(new WazaiActivity(
                "clinic-001",
                "Tokyo Taiwan Clinic (東京台灣診所)",
                "Internal medicine with Mandarin support",
                "https://tokyo-taiwan-clinic.jp",
                35.6812,  // Tokyo coordinates
                139.7671,
                null,     // Clinics don't have start time
                ActivityType.CLINIC,
                PROVIDER_NAME
            ));
        }

        return clinics;
    }

    private boolean containsSpecialty(String keyword, String... specialties) {
        if (keyword == null) return false;
        String lower = keyword.toLowerCase();
        for (String specialty : specialties) {
            if (lower.contains(specialty.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
```

### Step 2: That's It!

**No other changes needed.** Spring will automatically:

1. Detect the new `@Service` annotated class
2. Recognize it implements `ActivityProvider`
3. Inject it into `WazaiSearchService`
4. Include its results in search responses

### Step 3: Test It

```bash
# Start the application
./gradlew bootRun

# Check providers (should now show 3 providers)
curl http://localhost:8080/api/search/providers

# Response:
# {
#   "providers": ["Connpass", "Taiwan Tech Community", "Medical Clinics"]
# }

# Search for clinics
curl "http://localhost:8080/api/search?keyword=internal"

# Response includes results from all providers matching "internal"
```

## Real-World Integration Examples

### Example 1: Database-Backed Provider

```java
@Service
public class DatabaseClinicProvider implements ActivityProvider {

    private final ClinicRepository repository;

    public DatabaseClinicProvider(ClinicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WazaiActivity> search(String keyword) {
        return repository.findByKeyword(keyword)
            .stream()
            .map(this::toWazaiActivity)
            .toList();
    }

    private WazaiActivity toWazaiActivity(Clinic clinic) {
        return new WazaiActivity(
            "db-clinic-" + clinic.getId(),
            clinic.getName(),
            clinic.getDescription(),
            clinic.getWebsite(),
            clinic.getLatitude(),
            clinic.getLongitude(),
            null,
            ActivityType.CLINIC,
            "Internal Database"
        );
    }
}
```

### Example 2: External API Provider with Caching

```java
@Service
public class GooglePlacesProvider implements ActivityProvider {

    private final RestClient restClient;
    private final Cache cache;

    @Override
    public List<WazaiActivity> search(String keyword) {
        // Check cache first
        List<WazaiActivity> cached = cache.get(keyword);
        if (cached != null) return cached;

        // Call Google Places API
        GooglePlacesResponse response = restClient.get()
            .uri("/places/search?query=" + keyword)
            .retrieve()
            .body(GooglePlacesResponse.class);

        // Transform and cache
        List<WazaiActivity> activities = transformResponse(response);
        cache.put(keyword, activities);
        return activities;
    }
}
```

## Benefits Demonstrated

1. **Zero modification to existing code** - just add a new file
2. **Automatic integration** - Spring's dependency injection handles everything
3. **Consistent interface** - all providers look the same to consumers
4. **Independent testing** - each provider can be tested in isolation
5. **Fail-safe** - if one provider fails, others continue working

## Interview Showcase

> "In my Wazai project, I implemented a **provider pattern** architecture that allows adding new data sources by simply creating a new class implementing the `ActivityProvider` interface. For example, I added Connpass events, Taiwan tech communities, and medical clinics as separate providers. Thanks to Spring's dependency injection, the aggregator service automatically discovers and coordinates all providers without any configuration. This demonstrates the **Open-Closed Principle** - the system is open for extension but closed for modification."
