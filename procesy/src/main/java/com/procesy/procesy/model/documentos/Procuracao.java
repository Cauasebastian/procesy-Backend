package com.procesy.procesy.model.documentos;

import jakarta.persistence.*;

@Entity
@Table(name = "procuracao")
public class Procuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    // Construtores
    public Procuracao() {
    }

    public Procuracao(byte[] arquivo) {
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
