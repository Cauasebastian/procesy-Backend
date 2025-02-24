package com.procesy.procesy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoDTO {
    private Long id;
    private String nomeArquivo;
    private String tipoArquivo;

    public ContratoDTO(Long id, String nomeArquivo, String tipoArquivo) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.tipoArquivo = tipoArquivo;
    }
}
