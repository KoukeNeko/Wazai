package dev.koukeneko.wazai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration // 標記這是設定檔
public class AppConfig {

    @Bean // 手動產生一個 Builder Bean 給 Spring 管理
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}