package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "processo")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Número do processo é obrigatório")
    @Column(name = "numero_processo", nullable = false, unique = true)
    private String numeroProcesso;

    @NotBlank(message = "Tipo de processo é obrigatório")
    @Column(name = "tipo_processo", nullable = false)
    private String tipoProcesso;

    @NotBlank(message = "Tipo de atendimento é obrigatório")
    @Column(name = "tipo_atendimento", nullable = false)
    private String tipoAtendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    //advoagado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_id", nullable = false)
    private Advogado advogado;

    @Temporal(TemporalType.DATE)
    @Column(name = "data_inicio")
    private Date dataInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "data_atualizacao")
    private Date dataAtualizacao;


    @NotBlank(message = "Status é obrigatório")
    @Column(name = "status")
    private String status;


    @OneToOne(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentoProcesso documentoProcesso;

    public Processo() {}

    // Outros métodos, se necessário
}
