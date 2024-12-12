package com.procesy.procesy.dto;

public class ClienteDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private int quantidadeProcessos;

    // Construtores
    public ClienteDTO() {}

    public ClienteDTO(Long id, String nome, String email, String telefone, int quantidadeProcessos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.quantidadeProcessos = quantidadeProcessos;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getQuantidadeProcessos() {
        return quantidadeProcessos;
    }

    public void setQuantidadeProcessos(int quantidadeProcessos) {
        this.quantidadeProcessos = quantidadeProcessos;
    }
}
