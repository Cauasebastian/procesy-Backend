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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar operações relacionadas a DocumentoProcesso e seus documentos associados.
 */
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

    /**
     * Adiciona ou atualiza documentos a um Processo existente.
     *
     * @param processoId                     ID do Processo ao qual os documentos serão adicionados.
     * @param procuracoesFiles               Lista de arquivos de procuração.
     * @param peticoesIniciaisFiles          Lista de arquivos de petição inicial.
     * @param documentosComplementaresFiles  Lista de arquivos de documentos complementares.
     * @param contratosFiles                 Lista de arquivos de contratos.
     * @throws IOException                    Se ocorrer um erro ao ler os arquivos.
     * @throws IllegalArgumentException       Se o Processo com o ID fornecido não for encontrado.
     */
    @Transactional
    public void adicionarDocumentosAoProcesso(Long processoId,
                                              List<MultipartFile> procuracoesFiles,
                                              List<MultipartFile> peticoesIniciaisFiles,
                                              List<MultipartFile> documentosComplementaresFiles,
                                              List<MultipartFile> contratosFiles) throws IOException {

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo com ID " + processoId + " não encontrado."));

        DocumentoProcesso documentoProcesso = processo.getDocumentoProcesso();

        if (documentoProcesso == null) {
            // Se não existe DocumentoProcesso, cria um novo
            documentoProcesso = new DocumentoProcesso();
            documentoProcesso.setProcesso(processo);
            processo.setDocumentoProcesso(documentoProcesso);
        }

        // Limpar documentos existentes se necessário (opcional)
        documentoProcesso.getProcuracoes().clear();
        documentoProcesso.getPeticoesIniciais().clear();
        documentoProcesso.getDocumentosComplementares().clear();
        documentoProcesso.getContratos().clear(); // Limpar contratos existentes

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

        // Processar Contratos
        for (MultipartFile file : contratosFiles) {
            Contrato contrato = new Contrato(file.getBytes(), file.getOriginalFilename(), file.getContentType(), documentoProcesso.getStatusContrato());
            contrato.setDocumentoProcesso(documentoProcesso); // Vincular de volta
            documentoProcesso.getContratos().add(contrato);
        }

        // Salvar DocumentoProcesso (CascadeType.ALL cuidará de salvar DocumentoProcesso e seus documentos)
        documentoProcessoRepository.save(documentoProcesso);
    }

    /**
     * Recupera todos os documentos de um Processo específico como DTO.
     *
     * @param processoId ID do Processo.
     * @return DocumentoProcessoDTO contendo listas de IDs e metadados dos documentos, incluindo status.
     * @throws IllegalArgumentException Se o Processo com o ID fornecido não for encontrado.
     */
    @Transactional
    public DocumentoProcessoDTO getDocumentosDoProcessoDTO(Long processoId) {
        DocumentoProcesso documentoProcesso = documentoProcessoRepository.findByProcessoIdWithDocuments(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum DocumentoProcesso encontrado para o Processo com ID " + processoId));

        DocumentoProcessoDTO dto = new DocumentoProcessoDTO();
        dto.setId(documentoProcesso.getId());
        dto.setProcessoId(documentoProcesso.getProcesso().getId());

        // Setar os status de cada tipo de documento
        dto.setStatusContrato(documentoProcesso.getStatusContrato());
        dto.setStatusProcuracoes(documentoProcesso.getStatusProcuracoes());
        dto.setStatusPeticoesIniciais(documentoProcesso.getStatusPeticoesIniciais());
        dto.setStatusDocumentosComplementares(documentoProcesso.getStatusDocumentosComplementares());

        // Mapear Contratos
        Set<ContratoDTO> contratosDTO = documentoProcesso.getContratos().stream().map(contrato -> {
            ContratoDTO cDto = new ContratoDTO();
            cDto.setId(contrato.getId());
            cDto.setNomeArquivo(contrato.getNomeArquivo());
            cDto.setTipoArquivo(contrato.getTipoArquivo());
            return cDto;
        }).collect(Collectors.toSet());
        dto.setContratos(contratosDTO);

        // Mapear Procuracoes
        Set<ProcuracaoDTO> procuracoesDTO = documentoProcesso.getProcuracoes().stream().map(proc -> {
            ProcuracaoDTO pDto = new ProcuracaoDTO();
            pDto.setId(proc.getId());
            pDto.setNomeArquivo(proc.getNomeArquivo());
            pDto.setTipoArquivo(proc.getTipoArquivo());
            return pDto;
        }).collect(Collectors.toSet());
        dto.setProcuracoes(procuracoesDTO);

        // Mapear Peticoes Iniciais
        Set<PeticaoInicialDTO> peticoesDTO = documentoProcesso.getPeticoesIniciais().stream().map(pet -> {
            PeticaoInicialDTO piDto = new PeticaoInicialDTO();
            piDto.setId(pet.getId());
            piDto.setNomeArquivo(pet.getNomeArquivo());
            piDto.setTipoArquivo(pet.getTipoArquivo());
            return piDto;
        }).collect(Collectors.toSet());
        dto.setPeticoesIniciais(peticoesDTO);

        // Mapear Documentos Complementares
        Set<DocumentoComplementarDTO> documentosDTO = documentoProcesso.getDocumentosComplementares().stream().map(dc -> {
            DocumentoComplementarDTO dcDto = new DocumentoComplementarDTO();
            dcDto.setId(dc.getId());
            dcDto.setNomeArquivo(dc.getNomeArquivo());
            dcDto.setTipoArquivo(dc.getTipoArquivo());
            return dcDto;
        }).collect(Collectors.toSet());
        dto.setDocumentosComplementares(documentosDTO);

        return dto;
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

    /**
     * Recupera o arquivo de um Contrato específico.
     *
     * @param contratoId ID do Contrato.
     * @return Contrato com o arquivo.
     * @throws IllegalArgumentException Se o Contrato com o ID fornecido não for encontrado.
     */
    public Contrato getContratoById(Long contratoId) {
        Optional<Contrato> optionalContrato = contratoRepository.findById(contratoId);
        if (!optionalContrato.isPresent()) {
            throw new IllegalArgumentException("Contrato com ID " + contratoId + " não encontrado.");
        }
        return optionalContrato.get();
    }
}
