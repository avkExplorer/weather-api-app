package com.weather.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8081");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Weather API Support");
        contact.setEmail("support@weatherapi.com");
        contact.setUrl("http://localhost:8081");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Weather API")
                .version("1.0.0")
                .description(
                    "REST API that integrates with OpenWeatherMap to fetch live weather data and " +
                    "provides full CRUD operations on stored weather records via an in-memory store. " +
                    "Use GET /api/weather/city/{city} to fetch live data, then manage records with " +
                    "POST / PUT / DELETE endpoints."
                )
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
