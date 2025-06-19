package com.procesy.procesy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLResponseDTO {

    @JsonProperty("tempo_resposta_dias")
    private double tempoRespostaDias;

    // Getter
    public double getTempoRespostaDias() {
        return tempoRespostaDias;
    }

    // Setter
    public void setTempoRespostaDias(double tempoRespostaDias) {
        this.tempoRespostaDias = tempoRespostaDias;
    }
}