package com.procesy.procesy.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import java.util.List;

@Entity
@Table(name = "documento_cliente")
@lombok.Data
public class DocumentoCliente {

    @jakarta.persistence.Id
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
    private List<byte[]> cpfsTerceiros;

    @ElementCollection
    @CollectionTable(
            name = "rg_terceiros",
            joinColumns = @JoinColumn(name = "documento_cliente_id")
    )
    @Column(name = "rg")
    private List<byte[]> rgsTerceiros;
}
