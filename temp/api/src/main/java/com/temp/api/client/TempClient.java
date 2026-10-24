package com.temp.api.client;

import com.temp.api.dto.TempResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TempClient {

    private final RestClient restClient;
    private final String apiKey;

    public TempClient(
            RestClient.Builder builder,
            @Value("${openweather.api-key}") String apiKey) {

        this.restClient = builder
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();

        this.apiKey = apiKey;
    }

    public TempResponseDTO consultarTemperatura(
            double lat, double lon) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "pt_br")
                        .build())
                .retrieve()
                .body(TempResponseDTO.class);
    }
}