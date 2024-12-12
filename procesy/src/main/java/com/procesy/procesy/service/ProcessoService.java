package com.procesy.procesy.service;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.dto.ProcessoDTO;
import com.procesy.procesy.exception.AccessDeniedException;
import com.procesy.procesy.exception.ResourceNotFoundException;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar operações relacionadas a Processos.
 */
@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AdvogadoService advogadoService;

    /**
     * Retorna todos os Processos associados a um Advogado específico.
     *
     * @param advogadoId ID do Advogado
     * @return Lista de ProcessoDTO
     */
    public List<ProcessoDTO> getProcessosByAdvogadoId(Long advogadoId) {
        Advogado advogado = advogadoService.findById(advogadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Advogado não encontrado"));

        // Busca todos os Processos diretamente relacionados ao Advogado
        List<Processo> processos = processoRepository.findByClienteAdvogadoId(advogadoId);

        // Mapeia Processos para ProcessoDTO
        return processos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte uma entidade Processo para ProcessoDTO.
     *
     * @param processo Entidade Processo
     * @return ProcessoDTO
     */
    private ProcessoDTO convertToDTO(Processo processo) {
        ProcessoDTO dto = new ProcessoDTO();
        dto.setId(processo.getId());
        dto.setNumeroProcesso(processo.getNumeroProcesso());
        dto.setTipoProcesso(processo.getTipoProcesso());
        dto.setTipoAtendimento(processo.getTipoAtendimento());
        dto.setDataInicio(String.valueOf(processo.getDataInicio()));
        dto.setDataAtualizacao(String.valueOf(processo.getDataAtualizacao()));
        dto.setStatus(processo.getStatus());

        DocumentoProcesso dp = processo.getDocumentoProcesso();
        if (dp != null) {
            dto.setStatusContrato(dp.getStatusContrato());
            dto.setStatusProcuracoes(dp.getStatusProcuracoes());
            dto.setStatusPeticoesIniciais(dp.getStatusPeticoesIniciais());
            dto.setStatusDocumentosComplementares(dp.getStatusDocumentosComplementares());
        }

        // Mapeia Cliente para ClienteDTO
        Cliente cliente = processo.getCliente();
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(cliente.getId());
        clienteDTO.setNome(cliente.getNome());
        clienteDTO.setEmail(cliente.getEmail());
        clienteDTO.setTelefone(cliente.getTelefone());
        clienteDTO.setQuantidadeProcessos(cliente.getProcessos() != null ? cliente.getProcessos().size() : 0);
        dto.setCliente(clienteDTO);

        return dto;
    }

    /**
     * Cria um novo Processo associado a um Cliente e Advogado, incluindo os status dos documentos.
     *
     * @param processoDTO Dados do Processo e status dos documentos
     * @param advogadoId  ID do Advogado
     * @param clienteId   ID do Cliente
     * @return ProcessoDTO salvo
     */
    @Transactional
    public ProcessoDTO criarProcesso(ProcessoDTO processoDTO, Long advogadoId, Long clienteId) {
        // Verifica o cliente e se pertence ao advogado
        Cliente cliente = clienteService.getClienteById(clienteId, advogadoId);

        // Criar o Processo
        Processo processo = new Processo();
        processo.setNumeroProcesso(processoDTO.getNumeroProcesso());
        processo.setTipoProcesso(processoDTO.getTipoProcesso());
        processo.setTipoAtendimento(processoDTO.getTipoAtendimento());
        processo.setDataInicio(parseDate(processoDTO.getDataInicio()));
        processo.setDataAtualizacao(parseDate(processoDTO.getDataAtualizacao()));
        processo.setStatus(processoDTO.getStatus());
        processo.setCliente(cliente);

        // Criar DocumentoProcesso com os status
        DocumentoProcesso documentoProcesso = new DocumentoProcesso();
        documentoProcesso.setProcesso(processo);
        documentoProcesso.setStatusContrato(processoDTO.getStatusContrato());
        documentoProcesso.setStatusProcuracoes(processoDTO.getStatusProcuracoes());
        documentoProcesso.setStatusPeticoesIniciais(processoDTO.getStatusPeticoesIniciais());
        documentoProcesso.setStatusDocumentosComplementares(processoDTO.getStatusDocumentosComplementares());

        processo.setDocumentoProcesso(documentoProcesso);

        Processo processoSalvo = processoRepository.save(processo);

        return convertToDTO(processoSalvo);
    }

    /**
     * Retorna um Processo específico, garantindo que ele pertença ao Advogado.
     *
     * @param processoId ID do Processo
     * @param advogadoId ID do Advogado autenticado
     * @return ProcessoDTO encontrado
     */
    public ProcessoDTO getProcessoById(Long processoId, Long advogadoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o Processo pertence ao Advogado
        if (!processo.getCliente().getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Processo não pertence ao Advogado");
        }

        return convertToDTO(processo);
    }

    /**
     * Atualiza um Processo existente, garantindo que ele pertença ao Advogado e atualizando os status dos documentos.
     *
     * @param processoId        ID do Processo a ser atualizado
     * @param processoDTOAtualizado Dados atualizados do Processo e status dos documentos
     * @param advogadoId        ID do Advogado autenticado
     * @return ProcessoDTO atualizado
     */
    @Transactional
    public ProcessoDTO atualizarProcesso(Long processoId, ProcessoDTO processoDTOAtualizado, Long advogadoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o Processo pertence ao Advogado
        if (!processo.getCliente().getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Processo não pertence ao Advogado");
        }

        // Atualiza os campos necessários
        processo.setNumeroProcesso(processoDTOAtualizado.getNumeroProcesso());
        processo.setDataInicio(parseDate(processoDTOAtualizado.getDataInicio()));
        processo.setDataAtualizacao(parseDate(processoDTOAtualizado.getDataAtualizacao()));
        processo.setTipoProcesso(processoDTOAtualizado.getTipoProcesso());
        processo.setStatus(processoDTOAtualizado.getStatus());
        processo.setTipoAtendimento(processoDTOAtualizado.getTipoAtendimento());

        // Atualiza os status dos documentos
        DocumentoProcesso documentoProcesso = processo.getDocumentoProcesso();
        if (documentoProcesso == null) {
            documentoProcesso = new DocumentoProcesso();
            documentoProcesso.setProcesso(processo);
            processo.setDocumentoProcesso(documentoProcesso);
        }

        documentoProcesso.setStatusContrato(processoDTOAtualizado.getStatusContrato());
        documentoProcesso.setStatusProcuracoes(processoDTOAtualizado.getStatusProcuracoes());
        documentoProcesso.setStatusPeticoesIniciais(processoDTOAtualizado.getStatusPeticoesIniciais());
        documentoProcesso.setStatusDocumentosComplementares(processoDTOAtualizado.getStatusDocumentosComplementares());

        Processo processoAtualizado = processoRepository.save(processo);

        return convertToDTO(processoAtualizado);
    }

    /**
     * Deleta um Processo existente, garantindo que ele pertença ao Advogado.
     *
     * @param processoId ID do Processo a ser deletado
     * @param advogadoId ID do Advogado autenticado
     */
    @Transactional
    public void deletarProcesso(Long processoId, Long advogadoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o Processo pertence ao Advogado
        if (!processo.getCliente().getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Processo não pertence ao Advogado");
        }

        processoRepository.delete(processo);
    }

    /**
     * Método auxiliar para parse de datas no formato ISO8601.
     *
     * @param dateStr String da data no formato ISO8601
     * @return Instante correspondente
     */
    private Date parseDate(String dateStr) {
        try {
            return Date.from(Instant.parse(dateStr));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use ISO8601.");
        }
    }
}
