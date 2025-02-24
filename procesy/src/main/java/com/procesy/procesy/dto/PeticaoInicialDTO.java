package com.procesy.procesy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeticaoInicialDTO {
    private Long id;
    private String nomeArquivo;
    private String tipoArquivo;

    public PeticaoInicialDTO(Long id, String nomeArquivo, String tipoArquivo) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.tipoArquivo = tipoArquivo;
    }
}
