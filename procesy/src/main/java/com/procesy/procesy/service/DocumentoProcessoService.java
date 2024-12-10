package com.procesy.procesy.service;

import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.documentos.DocumentoComplementar;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import com.procesy.procesy.model.documentos.Procuracao;
import com.procesy.procesy.repository.ProcessoRepository;
import com.procesy.procesy.repository.documento.DocumentoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentoProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private DocumentoProcessoRepository documentoProcessoRepository;

    /**
     * Adiciona documentos a um Processo existente.
     *
     * @param processoId                 ID do Processo ao qual os documentos serão adicionados.
     * @param procuracoesFiles           Lista de arquivos de procuração.
     * @param peticoesIniciaisFiles      Lista de arquivos de petição inicial.
     * @param documentosComplementaresFiles Lista de arquivos de documentos complementares.
     * @throws IOException Se ocorrer um erro ao ler os arquivos.
     * @throws IllegalArgumentException Se o Processo com o ID fornecido não for encontrado.
     */
    @Transactional
    public void adicionarDocumentosAoProcesso(Long processoId,
                                              List<MultipartFile> procuracoesFiles,
                                              List<MultipartFile> peticoesIniciaisFiles,
                                              List<MultipartFile> documentosComplementaresFiles) throws IOException {

        Optional<Processo> optionalProcesso = processoRepository.findById(processoId);
        if (!optionalProcesso.isPresent()) {
            throw new IllegalArgumentException("Processo com ID " + processoId + " não encontrado.");
        }

        Processo processo = optionalProcesso.get();

        // Criar DocumentoProcesso
        DocumentoProcesso documentoProcesso = new DocumentoProcesso();
        documentoProcesso.setProcesso(processo);

        // Processar Procuracoes
        List<Procuracao> procuracoes = new ArrayList<>();
        for (MultipartFile file : procuracoesFiles) {
            Procuracao procuracao = new Procuracao(file.getBytes());
            procuracoes.add(procuracao);
        }
        documentoProcesso.setProcuracoes(procuracoes);

        // Processar Peticoes Iniciais
        List<PeticaoInicial> peticoesIniciais = new ArrayList<>();
        for (MultipartFile file : peticoesIniciaisFiles) {
            PeticaoInicial peticaoInicial = new PeticaoInicial(file.getBytes());
            peticoesIniciais.add(peticaoInicial);
        }
        documentoProcesso.setPeticoesIniciais(peticoesIniciais);

        // Processar Documentos Complementares
        List<DocumentoComplementar> documentosComplementares = new ArrayList<>();
        for (MultipartFile file : documentosComplementaresFiles) {
            DocumentoComplementar documentoComplementar = new DocumentoComplementar(file.getBytes());
            documentosComplementares.add(documentoComplementar);
        }
        documentoProcesso.setDocumentosComplementares(documentosComplementares);

        // Vincular DocumentoProcesso ao Processo
        List<DocumentoProcesso> documentoProcessos = processo.getDocumentoProcessos();
        if (documentoProcessos == null) {
            documentoProcessos = new ArrayList<>();
        }
        documentoProcessos.add(documentoProcesso);
        processo.setDocumentoProcessos(documentoProcessos);

        // Salvar DocumentoProcesso (devido ao CascadeType.ALL, isso também atualiza o Processo)
        documentoProcessoRepository.save(documentoProcesso);
    }
}
