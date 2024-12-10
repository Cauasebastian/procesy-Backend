package com.procesy.procesy.model.documentos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "procuracao")
public class Procuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    // Relação com DocumentoProcesso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_processo_id", nullable = false)
    @JsonBackReference("documentoProcesso-procuracoes")
    private DocumentoProcesso documentoProcesso;

    public Procuracao() {
    }

    public Procuracao(byte[] arquivo) {
        this.arquivo = arquivo;
    }
}
