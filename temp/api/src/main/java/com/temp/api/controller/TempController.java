
package com.temp.api.controller;

import com.temp.api.domain.service.TempService;
import com.temp.api.dto.TempResponseDTO;
import com.temp..api.domain.service.TempService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/temp")
public class TempController {

    private final TempService tempService;

    public TempController(TempService tempService) {
        this.tempService = tempService;
    }

    @GetMapping
    public TempResponseDTO consultarTemperatura(
            @RequestParam double lat,
            @RequestParam double lon) {

        // Validação das coordenadas geográficas
        if (!Double.isFinite(lat) || !Double.isFinite(lon)
                || lat < -90 || lat > 90
                || lon < -180 || lon > 180) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude ou longitude inválida."
            );
        }

        // Delegação da consulta ao service
        return tempService.consultarTemperatura(lat, lon);
    }
}
