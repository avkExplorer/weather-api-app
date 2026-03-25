package com.weather.controller;

import com.weather.model.ApiResponse;
import com.weather.model.WeatherData;
import com.weather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather/city/{city}
     * Fetch weather data from external API and store it
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse> getWeatherByCity(@PathVariable String city) {
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

    /**
     * GET /api/weather
     * Get all stored weather data
     */
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

    /**
     * GET /api/weather/{id}
     * Get weather data by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getWeatherById(@PathVariable Long id) {
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

    /**
     * POST /api/weather
     * Create new weather data
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createWeatherData(@RequestBody WeatherData weatherData) {
        try {
            WeatherData created = weatherService.createWeatherData(weatherData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Weather data created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/weather/{id}
     * Update existing weather data
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateWeatherData(
            @PathVariable Long id,
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

    /**
     * DELETE /api/weather/{id}
     * Delete weather data by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteWeatherData(@PathVariable Long id) {
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
