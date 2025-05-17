package com.procesy.procesy.service.cliente;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.dto.ProcessoDTO;
import com.procesy.procesy.exception.ResourceNotFoundException;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.repository.ProcessoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteProcessoService {

    private final ProcessoRepository processoRepository;

    public ClienteProcessoService(ProcessoRepository processoRepository) {
        this.processoRepository = processoRepository;
    }

    // GetClienteProcessos
    public List<ProcessoDTO> getClienteProcessos(String email) {

        List<Processo> processos = processoRepository.findByClienteEmail(email);

        // Mapeia Processos para ProcessoDTO
        return processos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // GetClienteProcessoById
    public ProcessoDTO getClienteProcessoById(Long id, String email) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o cliente tem acesso ao processo
        if (!processo.getCliente().getEmail().equals(email)) {
            throw new AccessDeniedException("Acesso negado ao processo");
        }

        return convertToDTO(processo);
    }



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
}
