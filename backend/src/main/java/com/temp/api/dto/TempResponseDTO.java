package com.temp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempResponseDTO {

    private double temperatura;
    private double sensacaoTermica;
    private int umidade;
    private String descricao;
    private String cidade;
}
