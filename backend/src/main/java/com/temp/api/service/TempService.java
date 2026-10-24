package com.temp.api.service;

import com.temp.api.client.TempClient;
import com.temp.api.dto.TempResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TempService {

    private final TempClient tempClient;

    public TempService(TempClient tempClient) {
        this.tempClient = tempClient;
    }

    public TempResponseDTO consultarTemperatura(
            double lat, double lon) {

        return tempClient.consultarTemperatura(lat, lon);
    }
}