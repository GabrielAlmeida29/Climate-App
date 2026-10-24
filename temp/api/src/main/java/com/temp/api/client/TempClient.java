package com.temp.api.client;

import com.temp.api.dto.OpenWeatherResponseDTO;
import com.temp.api.dto.TempResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TempClient {

    private final RestClient restClient;
    private final String apiKey;

    public TempClient(@Value("${openweather.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();
    }

    public TempResponseDTO consultarTemperatura(double lat, double lon) {

        OpenWeatherResponseDTO resposta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "pt_br")
                        .build())
                .retrieve()
                .body(OpenWeatherResponseDTO.class);

        if (resposta == null
                || resposta.getMain() == null
                || resposta.getWeather() == null
                || resposta.getWeather().length == 0) {

            throw new IllegalStateException(
                    "A OpenWeather retornou uma resposta incompleta.");
        }

        return new TempResponseDTO(
                resposta.getMain().getTemp(),
                resposta.getMain().getFeelsLike(),
                resposta.getMain().getHumidity(),
                resposta.getWeather()[0].getDescription(),
                resposta.getName()
        );
    }
}