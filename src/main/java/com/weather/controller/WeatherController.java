package com.weather.controller;

import com.weather.model.ApiResponse;
import com.weather.model.WeatherData;
import com.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather API", description = "Endpoints to fetch live weather from OpenWeatherMap and manage stored weather records via CRUD operations")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(
        summary = "Fetch weather by city from OpenWeatherMap",
        description = "Calls the external OpenWeatherMap API to retrieve current weather for the given city, stores the result locally, and returns it. Requires a valid API key in application.properties."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Weather data fetched and stored successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Weather data fetched successfully\",\"data\":{\"id\":1,\"city\":\"London\",\"country\":\"GB\",\"temperature\":14.5,\"humidity\":72,\"description\":\"overcast clouds\",\"windSpeed\":5.2,\"timestamp\":\"2026-03-24T10:30:00\"}}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "City not found",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Weather data not found for city: xyz\",\"data\":null}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "External API error or invalid API key",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Error: 401 Unauthorized\",\"data\":null}")))
    })
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse> getWeatherByCity(
            @Parameter(description = "Name of the city to fetch weather for", example = "London", required = true)
            @PathVariable String city) {
        try {
            WeatherData weatherData = weatherService.fetchWeatherByCity(city);
            if (weatherData != null) {
                return ResponseEntity.ok(
                        new ApiResponse(true, "Weather data fetched successfully", weatherData)
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Weather data not found for city: " + city));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Get all stored weather records",
        description = "Returns all weather data records currently held in the in-memory store. Records are accumulated from GET /city/{city} calls and manual POST creations."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of all stored weather records returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ApiResponse> getAllWeatherData() {
        try {
            List<WeatherData> allData = weatherService.getAllWeatherData();
            return ResponseEntity.ok(
                    new ApiResponse(true, "Weather data retrieved successfully", allData)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Get a weather record by ID",
        description = "Retrieves a single stored weather record by its local auto-assigned ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Weather record found and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No record with the given ID",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Weather data not found for ID: 99\",\"data\":null}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getWeatherById(
            @Parameter(description = "Local ID of the weather record", example = "1", required = true)
            @PathVariable Long id) {
        try {
            WeatherData weatherData = weatherService.getWeatherById(id);
            if (weatherData != null) {
                return ResponseEntity.ok(
                        new ApiResponse(true, "Weather data retrieved successfully", weatherData)
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Weather data not found for ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Create a weather record manually",
        description = "Manually add a weather data entry to the local in-memory store without calling the external API. Useful for testing or seeding data."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Weather record created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Weather data created successfully\",\"data\":{\"id\":3,\"city\":\"New York\",\"country\":\"US\",\"temperature\":18.5,\"humidity\":60,\"description\":\"partly cloudy\",\"windSpeed\":4.7,\"timestamp\":\"2026-03-24T11:00:00\"}}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse> createWeatherData(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Weather data to create. ID and timestamp are auto-assigned if not provided.",
                required = true,
                content = @Content(schema = @Schema(implementation = WeatherData.class),
                    examples = @ExampleObject(value = "{\"city\":\"New York\",\"country\":\"US\",\"temperature\":18.5,\"humidity\":60,\"description\":\"partly cloudy\",\"windSpeed\":4.7}"))
            )
            @RequestBody WeatherData weatherData) {
        try {
            WeatherData created = weatherService.createWeatherData(weatherData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Weather data created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Update an existing weather record",
        description = "Updates a weather record identified by ID. The ID in the path always takes precedence. Timestamp is refreshed if not supplied."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Weather record updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No record with the given ID",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Weather data not found for ID: 99\",\"data\":null}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateWeatherData(
            @Parameter(description = "Local ID of the weather record to update", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated weather data fields",
                required = true,
                content = @Content(schema = @Schema(implementation = WeatherData.class),
                    examples = @ExampleObject(value = "{\"city\":\"New York\",\"country\":\"US\",\"temperature\":21.0,\"humidity\":55,\"description\":\"sunny\",\"windSpeed\":3.2}"))
            )
            @RequestBody WeatherData weatherData) {
        try {
            WeatherData updated = weatherService.updateWeatherData(id, weatherData);
            if (updated != null) {
                return ResponseEntity.ok(
                        new ApiResponse(true, "Weather data updated successfully", updated)
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Weather data not found for ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Delete a weather record",
        description = "Permanently removes a weather record from the in-memory store by its local ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Weather record deleted successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Weather data deleted successfully\",\"data\":null}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No record with the given ID",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Weather data not found for ID: 99\",\"data\":null}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteWeatherData(
            @Parameter(description = "Local ID of the weather record to delete", example = "1", required = true)
            @PathVariable Long id) {
        try {
            boolean deleted = weatherService.deleteWeatherData(id);
            if (deleted) {
                return ResponseEntity.ok(
                        new ApiResponse(true, "Weather data deleted successfully")
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Weather data not found for ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }
}
