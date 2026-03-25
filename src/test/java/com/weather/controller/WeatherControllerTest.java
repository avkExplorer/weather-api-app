package com.weather.controller;

import com.weather.model.WeatherData;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    private WeatherData sampleWeatherData;

    @BeforeEach
    public void setUp() {
        sampleWeatherData = new WeatherData(
                1L,
                "London",
                "GB",
                15.5,
                72,
                "partly cloudy",
                3.2,
                LocalDateTime.now()
        );
    }

    @Test
    public void testGetWeatherByCity_Success() throws Exception {
        when(weatherService.fetchWeatherByCity("London")).thenReturn(sampleWeatherData);

        mockMvc.perform(get("/api/weather/city/London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.data.city", is("London")))
                .andExpect(jsonPath("$.data.temperature", is(15.5)));

        verify(weatherService, times(1)).fetchWeatherByCity("London");
    }

    @Test
    public void testGetAllWeatherData() throws Exception {
        List<WeatherData> weatherList = new ArrayList<>();
        weatherList.add(sampleWeatherData);
        when(weatherService.getAllWeatherData()).thenReturn(weatherList);

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].city", is("London")));

        verify(weatherService, times(1)).getAllWeatherData();
    }

    @Test
    public void testGetWeatherById_Success() throws Exception {
        when(weatherService.getWeatherById(1L)).thenReturn(sampleWeatherData);

        mockMvc.perform(get("/api/weather/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.city", is("London")));

        verify(weatherService, times(1)).getWeatherById(1L);
    }

    @Test
    public void testGetWeatherById_NotFound() throws Exception {
        when(weatherService.getWeatherById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/weather/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("not found")));

        verify(weatherService, times(1)).getWeatherById(999L);
    }

    @Test
    public void testCreateWeatherData() throws Exception {
        WeatherData newData = new WeatherData(
                null,
                "Paris",
                "FR",
                18.5,
                65,
                "sunny",
                2.1,
                null
        );
        WeatherData createdData = new WeatherData(
                2L,
                "Paris",
                "FR",
                18.5,
                65,
                "sunny",
                2.1,
                LocalDateTime.now()
        );

        when(weatherService.createWeatherData(ArgumentMatchers.any(WeatherData.class))).thenReturn(createdData);

        String jsonContent = "{\"city\":\"Paris\",\"country\":\"FR\",\"temperature\":18.5," +
                "\"humidity\":65,\"description\":\"sunny\",\"windSpeed\":2.1}";

        mockMvc.perform(post("/api/weather")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.city", is("Paris")));

        verify(weatherService, times(1)).createWeatherData(ArgumentMatchers.any(WeatherData.class));
    }

    @Test
    public void testUpdateWeatherData_Success() throws Exception {
        WeatherData updatedData = new WeatherData(
                1L,
                "London",
                "GB",
                16.2,
                68,
                "rainy",
                4.5,
                LocalDateTime.now()
        );

        when(weatherService.updateWeatherData(eq(1L), ArgumentMatchers.any(WeatherData.class))).thenReturn(updatedData);

        String jsonContent = "{\"city\":\"London\",\"country\":\"GB\",\"temperature\":16.2," +
                "\"humidity\":68,\"description\":\"rainy\",\"windSpeed\":4.5}";

        mockMvc.perform(put("/api/weather/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.temperature", is(16.2)))
                .andExpect(jsonPath("$.data.description", is("rainy")));

        verify(weatherService, times(1)).updateWeatherData(eq(1L), ArgumentMatchers.any(WeatherData.class));
    }

    @Test
    public void testUpdateWeatherData_NotFound() throws Exception {
        when(weatherService.updateWeatherData(eq(999L), ArgumentMatchers.any(WeatherData.class))).thenReturn(null);

        String jsonContent = "{\"city\":\"Unknown\",\"country\":\"XX\",\"temperature\":0," +
                "\"humidity\":0,\"description\":\"unknown\",\"windSpeed\":0}";

        mockMvc.perform(put("/api/weather/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));

        verify(weatherService, times(1)).updateWeatherData(eq(999L), ArgumentMatchers.any(WeatherData.class));
    }

    @Test
    public void testDeleteWeatherData_Success() throws Exception {
        when(weatherService.deleteWeatherData(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/weather/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("deleted")));

        verify(weatherService, times(1)).deleteWeatherData(1L);
    }

    @Test
    public void testDeleteWeatherData_NotFound() throws Exception {
        when(weatherService.deleteWeatherData(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/weather/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("not found")));

        verify(weatherService, times(1)).deleteWeatherData(999L);
    }
}
