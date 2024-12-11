package com.procesy.procesy.service;

import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.documentos.DocumentoComplementar;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import com.procesy.procesy.model.documentos.Procuracao;
import com.procesy.procesy.repository.ProcessoRepository;
import com.procesy.procesy.repository.documento.DocumentoProcessoRepository;
import com.procesy.procesy.repository.documento.ProcuracaoRepository;
import com.procesy.procesy.repository.documento.PeticaoInicialRepository;
import com.procesy.procesy.repository.documento.DocumentoComplementarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentoProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private DocumentoProcessoRepository documentoProcessoRepository;

    @Autowired
    private ProcuracaoRepository procuracaoRepository;

    @Autowired
    private PeticaoInicialRepository peticaoInicialRepository;

    @Autowired
    private DocumentoComplementarRepository documentoComplementarRepository;

    /**
     * Adiciona documentos a um Processo existente.
     *
     * @param processoId                     ID do Processo ao qual os documentos serão adicionados.
     * @param procuracoesFiles               Lista de arquivos de procuração.
     * @param peticoesIniciaisFiles          Lista de arquivos de petição inicial.
     * @param documentosComplementaresFiles  Lista de arquivos de documentos complementares.
     * @throws IOException                    Se ocorrer um erro ao ler os arquivos.
     * @throws IllegalArgumentException       Se o Processo com o ID fornecido não for encontrado.
     */
    @Transactional
    public void adicionarDocumentosAoProcesso(Long processoId,
                                              List<MultipartFile> procuracoesFiles,
                                              List<MultipartFile> peticoesIniciaisFiles,
                                              List<MultipartFile> documentosComplementaresFiles) throws IOException {

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo com ID " + processoId + " não encontrado."));

        // Criar DocumentoProcesso
        DocumentoProcesso documentoProcesso = new DocumentoProcesso();
        documentoProcesso.setProcesso(processo);

        // Processar Procurações
        for (MultipartFile file : procuracoesFiles) {
            Procuracao procuracao = new Procuracao(file.getBytes(), file.getOriginalFilename(), file.getContentType());
            procuracao.setDocumentoProcesso(documentoProcesso); // Vincular de volta
            documentoProcesso.getProcuracoes().add(procuracao);
        }

        // Processar Petições Iniciais
        for (MultipartFile file : peticoesIniciaisFiles) {
            PeticaoInicial peticaoInicial = new PeticaoInicial(file.getBytes(), file.getOriginalFilename(), file.getContentType());
            peticaoInicial.setDocumentoProcesso(documentoProcesso); // Vincular de volta
            documentoProcesso.getPeticoesIniciais().add(peticaoInicial);
        }

        // Processar Documentos Complementares
        for (MultipartFile file : documentosComplementaresFiles) {
            DocumentoComplementar documentoComplementar = new DocumentoComplementar(file.getBytes(), file.getOriginalFilename(), file.getContentType());
            documentoComplementar.setDocumentoProcesso(documentoProcesso); // Vincular de volta
            documentoProcesso.getDocumentosComplementares().add(documentoComplementar);
        }

        // Vincular DocumentoProcesso ao Processo
        processo.getDocumentoProcessos().add(documentoProcesso);

        // Salvar DocumentoProcesso (CascadeType.ALL cuidará de salvar DocumentoProcesso e seus documentos)
        documentoProcessoRepository.save(documentoProcesso);
    }


    /**
     * Recupera todos os documentos de um Processo específico.
     *
     * @param processoId ID do Processo.
     * @return DocumentoProcesso contendo listas de documentos.
     * @throws IllegalArgumentException Se o Processo com o ID fornecido não for encontrado.
     */
    @Transactional
    public DocumentoProcesso getDocumentosDoProcesso(Long processoId) {
        return documentoProcessoRepository.findByProcessoIdWithDocuments(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum DocumentoProcesso encontrado para o Processo com ID " + processoId));
    }

    /**
     * Recupera o arquivo de um Procuracao específico.
     *
     * @param procuracaoId ID do Procuracao.
     * @return Procuracao com o arquivo.
     * @throws IllegalArgumentException Se o Procuracao com o ID fornecido não for encontrado.
     */
    public Procuracao getProcuracaoById(Long procuracaoId) {
        Optional<Procuracao> optionalProcuracao = procuracaoRepository.findById(procuracaoId);
        if (!optionalProcuracao.isPresent()) {
            throw new IllegalArgumentException("Procuração com ID " + procuracaoId + " não encontrada.");
        }
        return optionalProcuracao.get();
    }

    /**
     * Recupera o arquivo de uma PeticaoInicial específica.
     *
     * @param peticaoId ID da PeticaoInicial.
     * @return PeticaoInicial com o arquivo.
     * @throws IllegalArgumentException Se a PeticaoInicial com o ID fornecido não for encontrada.
     */
    public PeticaoInicial getPeticaoInicialById(Long peticaoId) {
        Optional<PeticaoInicial> optionalPeticao = peticaoInicialRepository.findById(peticaoId);
        if (!optionalPeticao.isPresent()) {
            throw new IllegalArgumentException("Petição Inicial com ID " + peticaoId + " não encontrada.");
        }
        return optionalPeticao.get();
    }

    /**
     * Recupera o arquivo de um DocumentoComplementar específico.
     *
     * @param documentoComplementarId ID do DocumentoComplementar.
     * @return DocumentoComplementar com o arquivo.
     * @throws IllegalArgumentException Se o DocumentoComplementar com o ID fornecido não for encontrado.
     */
    public DocumentoComplementar getDocumentoComplementarById(Long documentoComplementarId) {
        Optional<DocumentoComplementar> optionalDocumento = documentoComplementarRepository.findById(documentoComplementarId);
        if (!optionalDocumento.isPresent()) {
            throw new IllegalArgumentException("Documento Complementar com ID " + documentoComplementarId + " não encontrado.");
        }
        return optionalDocumento.get();
    }
}
