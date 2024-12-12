package com.procesy.procesy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DocumentoProcessoDTO {
    private Long id;
    private Long processoId;

    // Novos campos de status
    private String statusContrato;
    private String statusProcuracoes;
    private String statusPeticoesIniciais;
    private String statusDocumentosComplementares;

    private Set<ProcuracaoDTO> procuracoes;
    private Set<PeticaoInicialDTO> peticoesIniciais;
    private Set<DocumentoComplementarDTO> documentosComplementares;
    private Set<ContratoDTO> contratos; // Se desejar incluir contratos
}
