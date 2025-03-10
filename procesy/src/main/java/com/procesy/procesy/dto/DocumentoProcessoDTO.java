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

    public DocumentoProcessoDTO(Long id, Long processoId, String statusContrato,
                                String statusProcuracoes, String statusPeticoesIniciais,
                                String statusDocumentosComplementares) {
        this.id = id;
        this.processoId = processoId;
        this.statusContrato = statusContrato;
        this.statusProcuracoes = statusProcuracoes;
        this.statusPeticoesIniciais = statusPeticoesIniciais;
        this.statusDocumentosComplementares = statusDocumentosComplementares;
    }
}
