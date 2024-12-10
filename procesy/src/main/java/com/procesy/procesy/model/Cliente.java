package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String genero;
    private String estadoCivil;
    private String cpf_cnpj;
    private String naturalidade;

    @Temporal(TemporalType.DATE)
    private Date dataNascimento;

    private String telefone;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_id", nullable = false)
    @JsonBackReference("advogado-clientes")
    private Advogado advogado;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("cliente-processos")
    private List<Processo> processos = new ArrayList<>();

    public Cliente(long l, String s) {
        this.id = l;
        this.nome = s;
    }

    public Cliente() {
    }
}
