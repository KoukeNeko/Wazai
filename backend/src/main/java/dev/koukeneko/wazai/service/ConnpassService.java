package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.ConnpassEvent;
import dev.koukeneko.wazai.dto.ConnpassResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service // This annotation indicates that this class is a service component in Spring, make Java manage its lifecycle
public class ConnpassService {

    @Value("${connpass.api.token:}") // Inject the value of connpass.api.token from application properties, default to empty string if not set
    private String apiToken;

    private final RestClient restClient;

    public ConnpassService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://connpass.com/api/v2").build();
    }

    //TODO: Update this method to match Connpass API v2 changes
    public ConnpassResponse searchEvents(String keyword) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/event/") // 這裡可能需要確認 v2 的路徑是否還是 /event/
                        .queryParam("keyword", keyword)
                        .queryParam("count", 10)
                        .build())
                // 2. 加入認證 Header (這是 v2 最常見的改變)
                // 請確認文件是要求 "Authorization: Bearer <token>" 還是 "X-Connpass-Token: <token>"
                // 這裡假設是標準的 Bearer Token
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(ConnpassResponse.class);
    }
}
