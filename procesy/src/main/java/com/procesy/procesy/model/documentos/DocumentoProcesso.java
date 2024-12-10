package com.procesy.procesy.model.documentos;

import com.procesy.procesy.model.DocumentoComplementar;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "documento_processo")
public class DocumentoProcesso {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Relação com Procuracao
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "documento_processo_id")
        private List<Procuracao> procuracoes;

        // Relação com PeticaoInicial
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "documento_processo_id")
        private List<PeticaoInicial> peticoesIniciais;

        // Relação com DocumentosComplementares
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "documento_processo_id")
        private List<DocumentoComplementar> documentosComplementares;

        // Construtores
        public DocumentoProcesso() {
        }

        public DocumentoProcesso(List<Procuracao> procuracoes, List<PeticaoInicial> peticoesIniciais,
                                 List<DocumentoComplementar> documentosComplementares) {
                this.procuracoes = procuracoes;
                this.peticoesIniciais = peticoesIniciais;
                this.documentosComplementares = documentosComplementares;
        }

        // Getters e Setters
        public Long getId() {
                return id;
        }

        public List<Procuracao> getProcuracoes() {
                return procuracoes;
        }

        public void setProcuracoes(List<Procuracao> procuracoes) {
                this.procuracoes = procuracoes;
        }

        public List<PeticaoInicial> getPeticoesIniciais() {
                return peticoesIniciais;
        }

        public void setPeticoesIniciais(List<PeticaoInicial> peticoesIniciais) {
                this.peticoesIniciais = peticoesIniciais;
        }

        public List<DocumentoComplementar> getDocumentosComplementares() {
                return documentosComplementares;
        }

        public void setDocumentosComplementares(List<DocumentoComplementar> documentosComplementares) {
                this.documentosComplementares = documentosComplementares;
        }
}
