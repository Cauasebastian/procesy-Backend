package com.procesy.procesy.model.documentos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.procesy.procesy.model.Processo;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "documento_processo")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DocumentoProcesso {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @EqualsAndHashCode.Include
        private Long id;

        // Relação com Processo
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "processo_id", nullable = false)
        private Processo processo;

        // Relação com Contrato
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Contrato> contratos = new HashSet<>();

        // Relação com Procuracao
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Procuracao> procuracoes = new HashSet<>();

        // Relação com PeticaoInicial
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PeticaoInicial> peticoesIniciais = new HashSet<>();

        // Relação com DocumentosComplementares
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<DocumentoComplementar> documentosComplementares = new HashSet<>();

        // Novos campos de status
        @Column(name = "status_contrato", nullable = false)
        private String statusContrato;

        @Column(name = "status_procuracoes", nullable = false)
        private String statusProcuracoes;

        @Column(name = "status_peticoes_iniciais", nullable = false)
        private String statusPeticoesIniciais;

        @Column(name = "status_documentos_complementares", nullable = false)
        private String statusDocumentosComplementares;

        public DocumentoProcesso() {}

        // Outros métodos, se necessário
}
