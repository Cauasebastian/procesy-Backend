package com.procesy.procesy.service;

import com.procesy.procesy.exception.AccessDeniedException;
import com.procesy.procesy.exception.ResourceNotFoundException;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * @return Lista de Processos
     */
    public List<Processo> getProcessosByAdvogadoId(Long advogadoId) {
        Advogado advogado = advogadoService.findById(advogadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Advogado não encontrado"));

        // Busca todos os Processos diretamente relacionados ao Advogado
        return processoRepository.findByClienteAdvogadoId(advogadoId);
    }

    /**
     * Cria um novo Processo associado a um Cliente e Advogado.
     *
     * @param processo   Dados do Processo
     * @param advogadoId ID do Advogado
     * @param clienteId  ID do Cliente
     * @return Processo salvo
     */
    @Transactional
    public Processo criarProcesso(Processo processo, Long advogadoId, Long clienteId) {
        // Busca o Cliente e verifica se pertence ao Advogado
        Cliente cliente = clienteService.getClienteById(clienteId, advogadoId);

        // Verifica se o Cliente pertence ao Advogado
        if (!cliente.getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Cliente não pertence ao Advogado");
        }

        processo.setCliente(cliente);
        return processoRepository.save(processo);
    }

    /**
     * Retorna um Processo específico, garantindo que ele pertença ao Advogado.
     *
     * @param processoId ID do Processo
     * @param advogadoId ID do Advogado autenticado
     * @return Processo encontrado
     */
    public Processo getProcessoById(Long processoId, Long advogadoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o Processo pertence ao Advogado
        if (!processo.getCliente().getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Processo não pertence ao Advogado");
        }

        return processo;
    }

    /**
     * Atualiza um Processo existente, garantindo que ele pertença ao Advogado.
     *
     * @param processoId         ID do Processo a ser atualizado
     * @param processoAtualizado Dados atualizados do Processo
     * @param advogadoId         ID do Advogado autenticado
     * @return Processo atualizado
     */
    @Transactional
    public Processo atualizarProcesso(Long processoId, Processo processoAtualizado, Long advogadoId) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Verifica se o Processo pertence ao Advogado
        if (!processo.getCliente().getAdvogado().getId().equals(advogadoId)) {
            throw new AccessDeniedException("Acesso negado: Processo não pertence ao Advogado");
        }

        // Atualiza os campos necessários
        processo.setNumeroProcesso(processoAtualizado.getNumeroProcesso());
        processo.setDataInicio(processoAtualizado.getDataInicio());
        processo.setDataAtualizacao(processoAtualizado.getDataAtualizacao());
        processo.setDataFim(processoAtualizado.getDataFim());
        processo.setStatus(processoAtualizado.getStatus());
        processo.setAcao(processoAtualizado.getAcao());

        return processoRepository.save(processo);
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
}
