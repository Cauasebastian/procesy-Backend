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
        // Relação com Procuracao
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Procuracao> procuracoes = new HashSet<>();

        // Relação com PeticaoInicial
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PeticaoInicial> peticoesIniciais = new HashSet<>();

        // Relação com DocumentosComplementares
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<DocumentoComplementar> documentosComplementares = new HashSet<>();

        public DocumentoProcesso() {}

        // Outros métodos, se necessário
}
