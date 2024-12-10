package com.procesy.procesy.model.documentos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "peticao_inicial")
public class PeticaoInicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    // Relação com DocumentoProcesso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_processo_id", nullable = false)
    @JsonBackReference("documentoProcesso-peticoesIniciais")
    private DocumentoProcesso documentoProcesso;

    public PeticaoInicial() {
    }

    public PeticaoInicial(byte[] arquivo) {
        this.arquivo = arquivo;
    }
}
