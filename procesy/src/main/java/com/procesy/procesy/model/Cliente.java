package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cliente")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    private String nome;
    private String genero;
    private String estadoCivil;
    private String cpf_cnpj;
    private String naturalidade;

    @Temporal(TemporalType.DATE)
    private Date dataNascimento;

    @Column(name = "senha", nullable = false)
    @NotEmpty(message = "A senha é obrigatória.")
    private String senha;

    private String telefone;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_id", nullable = false)
    private Advogado advogado;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Processo> processos = new ArrayList<>();

    public Cliente(UUID l, String s) {
        this.id = l;
        this.nome = s;
    }

    public Cliente() {
    }
}
