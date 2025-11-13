package az.dsa.chatbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("DSA Chatbot API")
                    .version(appVersion)
                    .description(
                        "Intelligent chatbot API for Data Science Academy\n\n" +
                        "**Features:**\n" +
                        "- Natural language processing\n" +
                        "- Multi-mode conversations (Contact, Consult, Query)\n" +
                        "- Training recommendations\n" +
                        "- Session management\n" +
                        "- Rate limiting\n\n" +
                        "**Contact Information:**\n" +
                        "- Phone: 051 341 43 40\n" +
                        "- Email: info@dsa.az\n" +
                        "- Website: https://dsa.az"
                    )
                    .contact(new Contact()
                        .name("DSA Support")
                        .email("info@dsa.az")
                        .url("https://dsa.az")
                    )
                    .license(new License()
                        .name("Proprietary")
                        .url("https://dsa.az/license")
                    )
                )
                .servers(Arrays.asList(
                    new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                    new Server()
                        .url("https://api.dsa.az")
                        .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("API Key"))
                .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("API Key", 
                        new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name("X-API-Key")
                            .description("API Key for authentication (Optional in development)")
                    )
                );
    }
}