package com.procesy.procesy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "documento_complementar")
public class DocumentoComplementar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    // Construtores
    public DocumentoComplementar() {
    }

    public DocumentoComplementar(byte[] arquivo) {
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
