package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "processo")
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Número do processo é obrigatório")
    @Column(name = "numero_processo", nullable = false, unique = true)
    private String numeroProcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonBackReference("cliente-processos")
    private Cliente cliente;

    @PastOrPresent(message = "Data de início deve estar no passado ou presente")
    @Temporal(TemporalType.DATE)
    @Column(name = "data_inicio")
    private Date dataInicio;

    @PastOrPresent(message = "Data de atualização deve estar no passado ou presente")
    @Temporal(TemporalType.DATE)
    @Column(name = "data_atualizacao")
    private Date dataAtualizacao;

    @FutureOrPresent(message = "Data de fim deve estar no futuro ou presente")
    @Temporal(TemporalType.DATE)
    @Column(name = "data_fim")
    private Date dataFim;

    @NotBlank(message = "Status é obrigatório")
    @Column(name = "status")
    private String status;

    @NotBlank(message = "Ação é obrigatória")
    @Column(name = "acao")
    private String acao;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("processo-documentoProcessos")
    private List<DocumentoProcesso> documentoProcessos = new ArrayList<>();

    public Processo() {

    }

    // Outros métodos, se necessário
}
