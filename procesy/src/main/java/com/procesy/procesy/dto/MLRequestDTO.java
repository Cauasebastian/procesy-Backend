package com.procesy.procesy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLRequestDTO {

    @JsonProperty("Tipo de Processo")
    private String tipoProcesso;

    @JsonProperty("Tipo de Atendimento")
    private String tipoAtendimento;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Tempo de Início (dias)")
    private long tempoInicioDias;

    @JsonProperty("Tempo de Atualização (dias)")
    private long tempoAtualizacaoDias;

    @JsonProperty("Status Contrato")
    private String statusContrato;

    @JsonProperty("Status Procuração")
    private String statusProcuracoes;

    @JsonProperty("Status Petição Inicial")
    private String statusPeticoesIniciais;

    @JsonProperty("Status Documento Complementar")
    private String statusDocumentosComplementares;

    // Getters e Setters
    public String getTipoProcesso() { return tipoProcesso; }
    public void setTipoProcesso(String tipoProcesso) { this.tipoProcesso = tipoProcesso; }

    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTempoInicioDias() { return tempoInicioDias; }
    public void setTempoInicioDias(long tempoInicioDias) { this.tempoInicioDias = tempoInicioDias; }

    public long getTempoAtualizacaoDias() { return tempoAtualizacaoDias; }
    public void setTempoAtualizacaoDias(long tempoAtualizacaoDias) { this.tempoAtualizacaoDias = tempoAtualizacaoDias; }

    public String getStatusContrato() { return statusContrato; }
    public void setStatusContrato(String statusContrato) { this.statusContrato = statusContrato; }

    public String getStatusProcuracoes() { return statusProcuracoes; }
    public void setStatusProcuracoes(String statusProcuracoes) { this.statusProcuracoes = statusProcuracoes; }

    public String getStatusPeticoesIniciais() { return statusPeticoesIniciais; }
    public void setStatusPeticoesIniciais(String statusPeticoesIniciais) { this.statusPeticoesIniciais = statusPeticoesIniciais; }

    public String getStatusDocumentosComplementares() { return statusDocumentosComplementares; }
    public void setStatusDocumentosComplementares(String statusDocumentosComplementares) { this.statusDocumentosComplementares = statusDocumentosComplementares; }
}