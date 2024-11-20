package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "cliente")
@Data
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

    @ManyToOne
    @JoinColumn(name = "advogado_id", nullable = false)
    @JsonBackReference
    private Advogado advogado;
}
