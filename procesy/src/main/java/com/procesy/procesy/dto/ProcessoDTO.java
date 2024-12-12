package com.procesy.procesy.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para transferência de dados do Processo, incluindo os status dos documentos.
 */
@Getter
@Setter
public class ProcessoDTO {

    private Long id;
    private String numeroProcesso;
    private String tipoProcesso;
    private String tipoAtendimento;
    private String dataInicio; // Formato ISO8601
    private String dataAtualizacao; // Formato ISO8601
    private String status;

    // Campos de status para cada tipo de documento
    private String statusContrato;
    private String statusProcuracoes;
    private String statusPeticoesIniciais;
    private String statusDocumentosComplementares;

    // Informações do Cliente
    private ClienteDTO cliente;
}
