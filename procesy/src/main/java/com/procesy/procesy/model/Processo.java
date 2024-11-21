package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
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
    @JsonBackReference
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
}