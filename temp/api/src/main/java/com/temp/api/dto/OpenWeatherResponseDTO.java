package com.temp.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public static class Main {

    private double temp;

    @JsonProperty("feels_like")
    private double feelsLike;

    private int humidity;
}

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public static class Weather {

    private String description;
}
