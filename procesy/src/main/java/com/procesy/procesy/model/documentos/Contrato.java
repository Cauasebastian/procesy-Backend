package com.procesy.procesy.model.documentos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contrato")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Lob
    @Column(name = "arquivo", nullable = false, columnDefinition = "MEDIUMBLOB")
    @JsonIgnore
    private byte[] arquivo;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "tipo_arquivo", nullable = false)
    private String tipoArquivo;


    // Relação com DocumentoProcesso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_processo_id", nullable = false)
    private DocumentoProcesso documentoProcesso;

    // Campos para criptografia
    @Lob
    @Column(name = "encrypted_key", columnDefinition = "MEDIUMBLOB")
    @JsonIgnore
    private byte[] encryptedKey;

    @Lob
    @Column(name = "iv", columnDefinition = "BLOB")
    @JsonIgnore
    private byte[] iv;

    public Contrato() {}

    public Contrato(byte[] arquivo, String nomeArquivo, String tipoArquivo) {
        this.arquivo = arquivo;
        this.nomeArquivo = nomeArquivo;
        this.tipoArquivo = tipoArquivo;
    }

    public Contrato(byte[] bytes, String originalFilename, String contentType, String statusContrato) {
        this.arquivo = bytes;
        this.nomeArquivo = originalFilename;
        this.tipoArquivo = contentType;
    }
}
