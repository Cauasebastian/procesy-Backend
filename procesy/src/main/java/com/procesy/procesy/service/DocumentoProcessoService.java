package com.procesy.procesy.service;

import com.procesy.procesy.dto.ContratoDTO;
import com.procesy.procesy.dto.DocumentoComplementarDTO;
import com.procesy.procesy.dto.DocumentoProcessoDTO;
import com.procesy.procesy.dto.PeticaoInicialDTO;
import com.procesy.procesy.dto.ProcuracaoDTO;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.documentos.Contrato;
import com.procesy.procesy.model.documentos.DocumentoComplementar;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import com.procesy.procesy.model.documentos.Procuracao;
import com.procesy.procesy.repository.ProcessoRepository;
import com.procesy.procesy.repository.documento.ContratoRepository;
import com.procesy.procesy.repository.documento.DocumentoComplementarRepository;
import com.procesy.procesy.repository.documento.DocumentoProcessoRepository;
import com.procesy.procesy.repository.documento.PeticaoInicialRepository;
import com.procesy.procesy.repository.documento.ProcuracaoRepository;
import com.procesy.procesy.security.Encription.FileCryptoUtil;
import com.procesy.procesy.security.Encription.PrivateKeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.hibernate.id.SequenceMismatchStrategy.LOG;
import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

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
    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private OpenAIAssistantService openAIAssistantService;

    // Executor global para processamento paralelo. Ajuste o tamanho do pool conforme os recursos disponíveis.
    private final ExecutorService globalExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Transactional
    public void adicionarDocumentosAoProcesso(Long processoId,
                                              List<MultipartFile> procuracoesFiles,
                                              List<MultipartFile> peticoesIniciaisFiles,
                                              List<MultipartFile> documentosComplementaresFiles,
                                              List<MultipartFile> contratosFiles) throws Exception {

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        // Obtém a chave pública do advogado associado ao processo
        byte[] publicKeyBytes = processo.getAdvogado().getPublicKey();
        PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        DocumentoProcesso documentoProcesso = initializeDocumentoProcesso(processo);

        List<Future<?>> futures = new ArrayList<>();

        if (!procuracoesFiles.isEmpty()) {
            futures.add(globalExecutor.submit(() ->
                    processarDocumentos(procuracoesFiles, publicKey, documentoProcesso, "Procuracao")
            ));
        }
        if (!peticoesIniciaisFiles.isEmpty()) {
            futures.add(globalExecutor.submit(() ->
                    processarDocumentos(peticoesIniciaisFiles, publicKey, documentoProcesso, "PeticaoInicial")
            ));
        }
        if (!documentosComplementaresFiles.isEmpty()) {
            futures.add(globalExecutor.submit(() ->
                    processarDocumentos(documentosComplementaresFiles, publicKey, documentoProcesso, "DocumentoComplementar")
            ));
        }
        if (!contratosFiles.isEmpty()) {
            futures.add(globalExecutor.submit(() ->
                    processarContratos(contratosFiles, publicKey, documentoProcesso)
            ));
        }

        try {
            for (Future<?> future : futures) {
                future.get(); // Isso agora propagará exceções
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Erro durante processamento paralelo: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento paralelo de documentos", e);
        }

        // Salvar tudo uma única vez após processamento completo
        documentoProcessoRepository.save(documentoProcesso);
        processoRepository.save(processo);
    }

    private DocumentoProcesso initializeDocumentoProcesso(Processo processo) {
        DocumentoProcesso documentoProcesso = processo.getDocumentoProcesso();
        if (documentoProcesso == null) {
            documentoProcesso = new DocumentoProcesso();
            documentoProcesso.setProcesso(processo);
            processo.setDocumentoProcesso(documentoProcesso);
        }
        return documentoProcesso;
    }

    private void clearExistingDocuments(DocumentoProcesso documentoProcesso) {
        documentoProcesso.getProcuracoes().clear();
        documentoProcesso.getPeticoesIniciais().clear();
        documentoProcesso.getDocumentosComplementares().clear();
        documentoProcesso.getContratos().clear();
    }

    private void processarDocumentos(List<MultipartFile> files,
                                     PublicKey publicKey,
                                     DocumentoProcesso documentoProcesso,
                                     String tipoDocumento) {
        try {
            Processo processo = documentoProcesso.getProcesso();
            String advogadoNome = processo.getAdvogado().getNome();

            // Obter IDs do Processo e Cliente
            Long idProcesso = processo.getId();

            LOGGER.info("ID do Processo: " + idProcesso);
            UUID idCliente = processo.getCliente().getId(); // Acesso direto via relação
            LOGGER.info("ID do Cliente: " + idCliente);

            String vectorStoreId = openAIAssistantService.getOrCreateVectorStore(advogadoNome);

            for (MultipartFile file : files) {
                try {
                    byte[] fileBytes = file.getBytes();
                    String originalFilename = file.getOriginalFilename();

                    // Formatar novo nome
                    String novoNome = String.format("%d_%s_%s",
                            idProcesso,
                            idCliente.toString(), // UUID deve ser convertido para String
                            originalFilename);

                    LOGGER.info("Novo nome do arquivo: " + novoNome);
                    FileCryptoUtil.EncryptedFileData encryptedData =
                            FileCryptoUtil.encryptFile(fileBytes, publicKey);
                    criarEntidadeDocumento(encryptedData, file, tipoDocumento, documentoProcesso);

                    LOGGER.info("Entidade " + tipoDocumento + " criada com sucesso.");

                    // Upload com novo nome
                    openAIAssistantService.uploadFileToVectorStore(
                            novoNome, // Nome formatado
                            fileBytes,
                            vectorStoreId
                    );
                    LOGGER.info("Arquivo enviado para o Vector Store com sucesso.");
                } catch (Exception ex) {
                    LOGGER.error("Erro crítico ao processar documentos do tipo " + tipoDocumento + ": " + ex.getMessage(), ex);
                    throw new RuntimeException("Falha no processamento de documentos", ex);
                }
            }
            salvarDocumentosEmLote(documentoProcesso, tipoDocumento);
        } catch (Exception ex) {
            // Tratamento de erro
            LOGGER.error("Erro ao processar documentos: " + ex.getMessage(), ex);
            throw new RuntimeException("Falha no processamento de documentos", ex);
        }
    }

    private void processarContratos(List<MultipartFile> files,
                                    PublicKey publicKey,
                                    DocumentoProcesso documentoProcesso) {
        try {
            Processo processo = documentoProcesso.getProcesso();
            Long idProcesso = processo.getId();
            UUID idCliente = processo.getCliente().getId();

            LOGGER.info("Iniciando processamento de contratos para processo {}" + idProcesso);

            List<CompletableFuture<Void>> futures = files.stream()
                    .map(file -> CompletableFuture.runAsync(() -> {
                        try {
                            byte[] fileBytes = file.getBytes();
                            String originalFilename = file.getOriginalFilename();

                            // Formatar nome CORRETO
                            String novoNome = String.format("%d_%s_%s",
                                    idProcesso,
                                    idCliente.toString(), // UUID convertido
                                    originalFilename);

                            LOGGER.debug("Processando contrato: " + novoNome);

                            FileCryptoUtil.EncryptedFileData encryptedData =
                                    FileCryptoUtil.encryptFile(fileBytes, publicKey);
                            criarEntidadeContrato(encryptedData, file, documentoProcesso);

                            String vectorStoreId = openAIAssistantService.getOrCreateVectorStore(
                                    processo.getAdvogado().getNome()
                            );

                            openAIAssistantService.uploadFileToVectorStore(
                                    novoNome,
                                    fileBytes,
                                    vectorStoreId
                            );
                        } catch (Exception e) {
                            LOGGER.error("Erro no contrato {}: {}" + file.getOriginalFilename(), e.getMessage(), e);
                            throw new CompletionException(e);
                        }
                    }, globalExecutor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            salvarDocumentosEmLote(documentoProcesso, "Contrato");

        } catch (Exception ex) {
            LOGGER.error("Falha crítica em contratos: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Identifica o tipo de documento e chama o método responsável pela sua criação.
     */
    private void criarEntidadeDocumento(FileCryptoUtil.EncryptedFileData encryptedData,
                                        MultipartFile file,
                                        String tipoDocumento,
                                        DocumentoProcesso documentoProcesso) {
        switch (tipoDocumento) {
            case "Procuracao":
                createAndAddProcuracao(documentoProcesso, encryptedData, file);
                break;
            case "PeticaoInicial":
                createAndAddPeticao(documentoProcesso, encryptedData, file);
                break;
            case "DocumentoComplementar":
                createAndAddDocumentoComplementar(documentoProcesso, encryptedData, file);
                break;
            case "Contrato":
                criarEntidadeContrato(encryptedData, file, documentoProcesso);
                break;
            default:
                throw new IllegalArgumentException("Tipo de documento inválido: " + tipoDocumento);
        }
    }

    // Operação de escrita em lote com nova transação
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void salvarDocumentosEmLote(DocumentoProcesso documentoProcesso, String tipoDocumento) {
        switch (tipoDocumento) {
            case "Contrato":
                contratoRepository.saveAll(documentoProcesso.getContratos());
                break;
            case "Procuracao":
                procuracaoRepository.saveAll(documentoProcesso.getProcuracoes());
                break;
            case "PeticaoInicial":
                peticaoInicialRepository.saveAll(documentoProcesso.getPeticoesIniciais());
                break;
            case "DocumentoComplementar":
                documentoComplementarRepository.saveAll(documentoProcesso.getDocumentosComplementares());
                break;
            default:
                throw new IllegalArgumentException("Tipo de documento não suportado");
        }
    }

    private void criarEntidadeContrato(FileCryptoUtil.EncryptedFileData encryptedData,
                                       MultipartFile file,
                                       DocumentoProcesso documentoProcesso) {
        Contrato contrato = new Contrato(
                encryptedData.getEncryptedData(),
                file.getOriginalFilename(),
                file.getContentType(),
                documentoProcesso.getStatusContrato()
        );
        contrato.setEncryptedKey(encryptedData.getEncryptedKey());
        contrato.setIv(encryptedData.getIv());
        contrato.setDocumentoProcesso(documentoProcesso);
        documentoProcesso.getContratos().add(contrato);
    }

    // Métodos para criação dos diferentes tipos de documentos

    private void createAndAddProcuracao(DocumentoProcesso documentoProcesso,
                                        FileCryptoUtil.EncryptedFileData data,
                                        MultipartFile file) {
        Procuracao procuracao = new Procuracao(
                data.getEncryptedData(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        procuracao.setEncryptedKey(data.getEncryptedKey());
        procuracao.setIv(data.getIv());
        procuracao.setDocumentoProcesso(documentoProcesso);
        documentoProcesso.getProcuracoes().add(procuracao);
    }

    private void createAndAddPeticao(DocumentoProcesso documentoProcesso,
                                     FileCryptoUtil.EncryptedFileData data,
                                     MultipartFile file) {
        PeticaoInicial peticao = new PeticaoInicial(
                data.getEncryptedData(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        peticao.setEncryptedKey(data.getEncryptedKey());
        peticao.setIv(data.getIv());
        peticao.setDocumentoProcesso(documentoProcesso);
        documentoProcesso.getPeticoesIniciais().add(peticao);
    }

    private void createAndAddDocumentoComplementar(DocumentoProcesso documentoProcesso,
                                                   FileCryptoUtil.EncryptedFileData data,
                                                   MultipartFile file) {
        DocumentoComplementar documento = new DocumentoComplementar(
                data.getEncryptedData(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        documento.setEncryptedKey(data.getEncryptedKey());
        documento.setIv(data.getIv());
        documento.setDocumentoProcesso(documentoProcesso);
        documentoProcesso.getDocumentosComplementares().add(documento);
    }

    // Métodos para obtenção e descriptografia dos documentos

    public Procuracao getProcuracaoById(Long procuracaoId) throws Exception {
        Procuracao procuracao = procuracaoRepository.findById(procuracaoId)
                .orElseThrow(() -> new IllegalArgumentException("Procuração não encontrada"));
        return decryptDocument(procuracao);
    }

    public PeticaoInicial getPeticaoInicialById(Long peticaoId) throws Exception {
        PeticaoInicial peticao = peticaoInicialRepository.findById(peticaoId)
                .orElseThrow(() -> new IllegalArgumentException("Petição não encontrada"));
        return decryptDocument(peticao);
    }

    public DocumentoComplementar getDocumentoComplementarById(Long documentoId) throws Exception {
        DocumentoComplementar documento = documentoComplementarRepository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado"));
        return decryptDocument(documento);
    }

    public Contrato getContratoById(Long contratoId) throws Exception {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        return decryptDocument(contrato);
    }

    private <T> T decryptDocument(T documento) throws Exception {
        PrivateKey privateKey = PrivateKeyHolder.getPrivateKey();
        if (privateKey == null) {
            throw new SecurityException("Chave privada não fornecida");
        }

        FileCryptoUtil.EncryptedFileData encryptedData = extractEncryptedData(documento);
        byte[] decryptedData = FileCryptoUtil.decryptFile(encryptedData, privateKey);
        return createDecryptedDocument(documento, decryptedData);
    }

    private FileCryptoUtil.EncryptedFileData extractEncryptedData(Object documento) {
        if (documento instanceof Procuracao) {
            Procuracao p = (Procuracao) documento;
            return new FileCryptoUtil.EncryptedFileData(p.getArquivo(), p.getEncryptedKey(), p.getIv());
        }
        if (documento instanceof PeticaoInicial) {
            PeticaoInicial p = (PeticaoInicial) documento;
            return new FileCryptoUtil.EncryptedFileData(p.getArquivo(), p.getEncryptedKey(), p.getIv());
        }
        if (documento instanceof DocumentoComplementar) {
            DocumentoComplementar d = (DocumentoComplementar) documento;
            return new FileCryptoUtil.EncryptedFileData(d.getArquivo(), d.getEncryptedKey(), d.getIv());
        }
        if (documento instanceof Contrato) {
            Contrato c = (Contrato) documento;
            return new FileCryptoUtil.EncryptedFileData(c.getArquivo(), c.getEncryptedKey(), c.getIv());
        }
        throw new IllegalArgumentException("Tipo de documento não suportado");
    }

    @SuppressWarnings("unchecked")
    private <T> T createDecryptedDocument(T original, byte[] decryptedData) {
        if (original instanceof Procuracao) {
            Procuracao p = (Procuracao) original;
            Procuracao decrypted = new Procuracao(decryptedData, p.getNomeArquivo(), p.getTipoArquivo());
            decrypted.setId(p.getId());
            return (T) decrypted;
        }
        if (original instanceof PeticaoInicial) {
            PeticaoInicial p = (PeticaoInicial) original;
            PeticaoInicial decrypted = new PeticaoInicial(decryptedData, p.getNomeArquivo(), p.getTipoArquivo());
            decrypted.setId(p.getId());
            return (T) decrypted;
        }
        if (original instanceof DocumentoComplementar) {
            DocumentoComplementar d = (DocumentoComplementar) original;
            DocumentoComplementar decrypted = new DocumentoComplementar(decryptedData, d.getNomeArquivo(), d.getTipoArquivo());
            decrypted.setId(d.getId());
            return (T) decrypted;
        }
        if (original instanceof Contrato) {
            Contrato c = (Contrato) original;
            Contrato decrypted = new Contrato(decryptedData, c.getNomeArquivo(), c.getTipoArquivo());
            decrypted.setId(c.getId());
            return (T) decrypted;
        }
        throw new IllegalArgumentException("Tipo de documento não suportado");
    }

    @Transactional
    public DocumentoProcessoDTO getDocumentosDoProcessoDTO(Long processoId) {
        DocumentoProcessoDTO dto = documentoProcessoRepository.findDocumentoProcessoDTOByProcessoId(processoId)
                .orElseThrow(() -> new IllegalArgumentException("DocumentoProcesso não encontrado"));

        dto.setProcuracoes(new HashSet<>(procuracaoRepository.findProcuracoesByDocumentoProcessoId(dto.getId())));
        dto.setPeticoesIniciais(new HashSet<>(peticaoInicialRepository.findPeticoesIniciaisByDocumentoProcessoId(dto.getId())));
        dto.setDocumentosComplementares(new HashSet<>(documentoComplementarRepository.findDocumentosComplementaresByDocumentoProcessoId(dto.getId())));
        dto.setContratos(new HashSet<>(contratoRepository.findContratosByDocumentoProcessoId(dto.getId())));

        return dto;
    }

    private Set<ContratoDTO> mapContratos(Set<Contrato> contratos) {
        return contratos.stream()
                .map(c -> new ContratoDTO(c.getId(), c.getNomeArquivo(), c.getTipoArquivo()))
                .collect(Collectors.toSet());
    }

    private Set<ProcuracaoDTO> mapProcuracoes(Set<Procuracao> procuracoes) {
        return procuracoes.stream()
                .map(p -> new ProcuracaoDTO(p.getId(), p.getNomeArquivo(), p.getTipoArquivo()))
                .collect(Collectors.toSet());
    }

    private Set<PeticaoInicialDTO> mapPeticoes(Set<PeticaoInicial> peticoes) {
        return peticoes.stream()
                .map(p -> new PeticaoInicialDTO(p.getId(), p.getNomeArquivo(), p.getTipoArquivo()))
                .collect(Collectors.toSet());
    }

    private Set<DocumentoComplementarDTO> mapDocumentosComplementares(Set<DocumentoComplementar> documentos) {
        return documentos.stream()
                .map(d -> new DocumentoComplementarDTO(d.getId(), d.getNomeArquivo(), d.getTipoArquivo()))
                .collect(Collectors.toSet());
    }
}