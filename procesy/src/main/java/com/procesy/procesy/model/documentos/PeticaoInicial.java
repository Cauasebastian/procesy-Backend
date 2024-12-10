package com.procesy.procesy.model.documentos;

import jakarta.persistence.*;

@Entity
@Table(name = "peticao_inicial")
public class PeticaoInicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    // Construtores
    public PeticaoInicial() {
    }

    public PeticaoInicial(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }
}
