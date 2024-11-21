package com.procesy.procesy.repository;

import com.procesy.procesy.model.Processo;
import com.procesy.procesy.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessoRepository extends JpaRepository<Processo, Long> {
    List<Processo> findByCliente(Cliente cliente);
    Optional<Processo> findByNumeroProcesso(String numeroProcesso);
    /**
     * Encontra todos os Processos associados aos Clientes de um Advogado específico.
     *
     * @param advogadoId ID do Advogado
     * @return Lista de Processos
     */
    List<Processo> findByClienteAdvogadoId(Long advogadoId);;
}
