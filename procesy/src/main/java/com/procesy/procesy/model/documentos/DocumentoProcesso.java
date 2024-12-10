package com.procesy.procesy.model.documentos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.procesy.procesy.model.Processo;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "documento_processo")
public class DocumentoProcesso {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Relação com Processo
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "processo_id", nullable = false)
        @JsonBackReference("processo-documentoProcessos")
        private Processo processo;

        // Relação com Procuracao
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("documentoProcesso-procuracoes")
        private List<Procuracao> procuracoes = new ArrayList<>();

        // Relação com PeticaoInicial
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("documentoProcesso-peticoesIniciais")
        private List<PeticaoInicial> peticoesIniciais = new ArrayList<>();

        // Relação com DocumentosComplementares
        @OneToMany(mappedBy = "documentoProcesso", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("documentoProcesso-documentosComplementares")
        private List<DocumentoComplementar> documentosComplementares = new ArrayList<>();

        public DocumentoProcesso() {

        }

        // Outros métodos, se necessário
}
