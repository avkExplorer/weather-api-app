package com.weather.service;

import com.weather.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class WeatherServiceTest {

    private WeatherService weatherService;
    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        webClient = mock(WebClient.class);
        weatherService = new WeatherService(webClient);
    }

    @Test
    public void testCreateWeatherData() {
        WeatherData data = new WeatherData(
                null,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                null
        );

        WeatherData created = weatherService.createWeatherData(data);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("London", created.getCity());
        assertEquals("GB", created.getCountry());
        assertEquals(15.5, created.getTemperature());
        assertNotNull(created.getTimestamp());
    }

    @Test
    public void testGetWeatherById() {
        WeatherData data = new WeatherData(
                null,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                null
        );

        WeatherData created = weatherService.createWeatherData(data);
        Long id = created.getId();

        WeatherData retrieved = weatherService.getWeatherById(id);

        assertNotNull(retrieved);
        assertEquals(id, retrieved.getId());
        assertEquals("London", retrieved.getCity());
    }

    @Test
    public void testGetAllWeatherData() {
        WeatherData data1 = new WeatherData(
                null,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                null
        );
        WeatherData data2 = new WeatherData(
                null,
                "Paris",
                "FR",
                18.5,
                65,
                "sunny",
                2.1,
                null
        );

        weatherService.createWeatherData(data1);
        weatherService.createWeatherData(data2);

        List<WeatherData> all = weatherService.getAllWeatherData();

        assertEquals(2, all.size());
    }

    @Test
    public void testUpdateWeatherData() {
        WeatherData original = new WeatherData(
                null,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                null
        );

        WeatherData created = weatherService.createWeatherData(original);
        Long id = created.getId();

        WeatherData updated = new WeatherData(
                null,
                "London",
                "GB",
                16.2,
                68,
                "rainy",
                4.5,
                null
        );

        WeatherData result = weatherService.updateWeatherData(id, updated);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(16.2, result.getTemperature());
        assertEquals("rainy", result.getDescription());
    }

    @Test
    public void testUpdateWeatherData_NotFound() {
        WeatherData data = new WeatherData(
                null,
                "Unknown",
                "XX",
                0.0,
                0,
                "unknown",
                0.0,
                null
        );

        WeatherData result = weatherService.updateWeatherData(999L, data);

        assertNull(result);
    }

    @Test
    public void testDeleteWeatherData() {
        WeatherData data = new WeatherData(
                null,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                null
        );

        WeatherData created = weatherService.createWeatherData(data);
        Long id = created.getId();

        boolean deleted = weatherService.deleteWeatherData(id);

        assertTrue(deleted);
        assertNull(weatherService.getWeatherById(id));
    }

    @Test
    public void testDeleteWeatherData_NotFound() {
        boolean deleted = weatherService.deleteWeatherData(999L);

        assertFalse(deleted);
    }

    @Test
    public void testIdGenerationIsUnique() {
        WeatherData data1 = new WeatherData(null, "London", "GB", 15.5, 72, "cloudy", 3.2, null);
        WeatherData data2 = new WeatherData(null, "Paris", "FR", 18.5, 65, "sunny", 2.1, null);

        WeatherData created1 = weatherService.createWeatherData(data1);
        WeatherData created2 = weatherService.createWeatherData(data2);

        assertNotEquals(created1.getId(), created2.getId());
    }

    @Test
    public void testThreadSafetyOfStore() {
        // Create multiple threads to test concurrent access
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                WeatherData data = new WeatherData(
                        null,
                        "City" + i,
                        "XX",
                        20.0,
                        50,
                        "test",
                        5.0,
                        null
                );
                weatherService.createWeatherData(data);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 10; i < 20; i++) {
                WeatherData data = new WeatherData(
                        null,
                        "City" + i,
                        "YY",
                        25.0,
                        60,
                        "test",
                        6.0,
                        null
                );
                weatherService.createWeatherData(data);
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        List<WeatherData> all = weatherService.getAllWeatherData();
        assertEquals(20, all.size());
    }
}
