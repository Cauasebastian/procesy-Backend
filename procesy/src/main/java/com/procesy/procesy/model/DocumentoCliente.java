package com.procesy.procesy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "documento_cliente")
public class DocumentoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "cpf", nullable = false)
    private byte[] cpf;

    @Lob
    @Column(name = "rg", nullable = false)
    private byte[] rg;

    @ElementCollection
    @CollectionTable(
            name = "cpf_terceiros",
            joinColumns = @JoinColumn(name = "documento_cliente_id")
    )
    @Column(name = "cpf")
    private List<byte[]> cpfsTerceiros = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "rg_terceiros",
            joinColumns = @JoinColumn(name = "documento_cliente_id")
    )
    @Column(name = "rg")
    private List<byte[]> rgsTerceiros = new ArrayList<>();
}
