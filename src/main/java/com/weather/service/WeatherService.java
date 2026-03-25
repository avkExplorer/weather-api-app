package com.weather.service;

import com.weather.model.WeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WeatherService {

    private final ConcurrentHashMap<Long, WeatherData> weatherStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final WebClient webClient;

    @Value("${weather.api.base-url}")
    private String apiBaseUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fetch weather data from OpenWeatherMap API for a given city
     */
    public WeatherData fetchWeatherByCity(String city) {
        try {
            String url = String.format("%s/weather?q=%s&appid=%s&units=metric", apiBaseUrl, city, apiKey);

            WeatherApiResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(WeatherApiResponse.class)
                    .block();

            if (response != null) {
                WeatherData weatherData = parseApiResponse(response, city);
                Long id = idGenerator.getAndIncrement();
                weatherData.setId(id);
                weatherStore.put(id, weatherData);
                return weatherData;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data for city: " + city + ". " + e.getMessage());
        }
        return null;
    }

    /**
     * Parse the OpenWeatherMap API response into WeatherData object
     */
    private WeatherData parseApiResponse(WeatherApiResponse response, String city) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(city);
        weatherData.setCountry(response.getSys() != null ? response.getSys().getCountry() : "Unknown");
        weatherData.setTemperature(response.getMain() != null ? response.getMain().getTemp() : 0.0);
        weatherData.setHumidity(response.getMain() != null ? response.getMain().getHumidity() : 0);
        weatherData.setWindSpeed(response.getWind() != null ? response.getWind().getSpeed() : 0.0);
        weatherData.setDescription(response.getWeather() != null && !response.getWeather().isEmpty()
                ? response.getWeather().get(0).getDescription()
                : "No description");
        weatherData.setTimestamp(LocalDateTime.now());
        return weatherData;
    }

    /**
     * Get all stored weather data
     */
    public List<WeatherData> getAllWeatherData() {
        return new ArrayList<>(weatherStore.values());
    }

    /**
     * Get weather data by ID
     */
    public WeatherData getWeatherById(Long id) {
        return weatherStore.get(id);
    }

    /**
     * Create new weather data manually
     */
    public WeatherData createWeatherData(WeatherData data) {
        if (data.getId() == null) {
            data.setId(idGenerator.getAndIncrement());
        }
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        weatherStore.put(data.getId(), data);
        return data;
    }

    /**
     * Update existing weather data
     */
    public WeatherData updateWeatherData(Long id, WeatherData data) {
        if (!weatherStore.containsKey(id)) {
            return null;
        }
        data.setId(id);
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        weatherStore.put(id, data);
        return data;
    }

    /**
     * Delete weather data by ID
     */
    public boolean deleteWeatherData(Long id) {
        return weatherStore.remove(id) != null;
    }

    /**
     * Internal class to map OpenWeatherMap API response
     */
    public static class WeatherApiResponse {
        private Main main;
        private List<Weather> weather;
        private Wind wind;
        private Sys sys;

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<Weather> weather) {
            this.weather = weather;
        }

        public Wind getWind() {
            return wind;
        }

        public void setWind(Wind wind) {
            this.wind = wind;
        }

        public Sys getSys() {
            return sys;
        }

        public void setSys(Sys sys) {
            this.sys = sys;
        }
    }

    public static class Main {
        private Double temp;
        private Integer humidity;

        public Double getTemp() {
            return temp;
        }

        public void setTemp(Double temp) {
            this.temp = temp;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }
    }

    public static class Weather {
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Wind {
        private Double speed;

        public Double getSpeed() {
            return speed;
        }

        public void setSpeed(Double speed) {
            this.speed = speed;
        }
    }

    public static class Sys {
        private String country;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
