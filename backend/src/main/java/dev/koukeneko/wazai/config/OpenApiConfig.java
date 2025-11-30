package dev.koukeneko.wazai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Wazai API documentation.
 * Provides interactive API documentation accessible at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wazaiOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers());
    }

    private Info createApiInfo() {
        return new Info()
                .title("Wazai API")
                .version("v1.0")
                .description("""
                        Wazai Platform REST API - Multi-source activity and location aggregation service.

                        ## Features
                        - 🗺️ **Multi-source aggregation**: Connpass, GDG Community, Taiwan Tech Community
                        - 🎯 **Dual data types**: Events (time-based) and Places (static locations)
                        - 🌏 **Geographic coverage**: Taiwan and Japan
                        - 🔍 **Smart search**: Bilingual keyword support (English + Chinese)

                        ## Data Sources
                        - **GDG Community**: Google Developer Groups events (DevFest, Study Jams, Workshops)
                        - **Taiwan Tech Community**: PyCon Taiwan, MOPCON, SITCON, HITCON, COSCUP, ModernWeb
                        - **Connpass**: Japan tech community events
                        """)
                .contact(new Contact()
                        .name("Wazai Team")
                        .email("contact@wazai.dev")
                        .url("https://wazai.dev"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Development Server"),
                new Server()
                        .url("https://api.wazai.dev")
                        .description("Production Server (Future)")
        );
    }
}
