package com.procesy.procesy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DocumentoProcessoDTO {
    private Long id;
    private Long processoId;
    private Set<ProcuracaoDTO> procuracoes;
    private Set<PeticaoInicialDTO> peticoesIniciais;
    private Set<DocumentoComplementarDTO> documentosComplementares;
}
