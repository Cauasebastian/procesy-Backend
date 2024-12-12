package com.procesy.procesy.controller;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.dto.ProcessoDTO;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.AdvogadoRepository;
import com.procesy.procesy.repository.ClienteRepository;
import com.procesy.procesy.service.ClienteService;
import com.procesy.procesy.service.ProcessoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/advogado")
public class AdvogadoController {

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProcessoService processoService;

    // ---------------------- Endpoints para Clientes ----------------------

    /**
     * Endpoint para obter os Clientes do Advogado autenticado.
     *
     * @param authentication Objeto de autenticação do Spring Security.
     * @return Lista de Clientes.
     */
    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> getMeusClientes(Authentication authentication) {
        String email = authentication.getName();
        Optional<Advogado> advogadoOpt = advogadoRepository.findByEmail(email);
        if (advogadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        Advogado advogado = advogadoOpt.get();
        List<Cliente> clientes = clienteRepository.findByAdvogado(advogado);

        // Converter para ClienteDTO
        List<ClienteDTO> clienteDTOs = clientes.stream()
                .map(clienteService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clienteDTOs);
    }

    /**
     * Endpoint para criar um novo Cliente.
     *
     * @param cliente        Dados do Cliente.
     * @param authentication Objeto de autenticação do Spring Security.
     * @return Cliente criado.
     */
    @PostMapping("/clientes")
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente, Authentication authentication) {
        String email = authentication.getName();
        Optional<Advogado> advogadoOpt = advogadoRepository.findByEmail(email);
        if (advogadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        Advogado advogado = advogadoOpt.get();
        cliente.setAdvogado(advogado);
        Cliente savedCliente = clienteRepository.save(cliente);
        return ResponseEntity.ok(savedCliente);
    }

    /**
     * Endpoint para atualizar um Cliente existente.
     *
     * @param id               ID do Cliente a ser atualizado.
     * @param clienteAtualizado Dados atualizados do Cliente.
     * @param authentication   Objeto de autenticação do Spring Security.
     * @return Cliente atualizado.
     */
    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente clienteAtualizado,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAdvogado().getId().equals(advogado.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Atualiza os campos necessários
        cliente.setNome(clienteAtualizado.getNome());
        cliente.setGenero(clienteAtualizado.getGenero());
        cliente.setEstadoCivil(clienteAtualizado.getEstadoCivil());
        cliente.setCpf_cnpj(clienteAtualizado.getCpf_cnpj());
        cliente.setNaturalidade(clienteAtualizado.getNaturalidade());
        cliente.setDataNascimento(clienteAtualizado.getDataNascimento());
        cliente.setTelefone(clienteAtualizado.getTelefone());
        cliente.setEmail(clienteAtualizado.getEmail());

        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.ok(salvo);
    }

    /**
     * Endpoint para deletar um Cliente.
     *
     * @param id             ID do Cliente a ser deletado.
     * @param authentication Objeto de autenticação do Spring Security.
     * @return Resposta vazia com status 204.
     */
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deletarCliente(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAdvogado().getId().equals(advogado.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        clienteRepository.delete(cliente);
        return ResponseEntity.noContent().build();
    }

    // ---------------------- Endpoints para Processos ----------------------

    /**
     * Endpoint para obter todos os Processos do Advogado autenticado.
     *
     * @param authentication Objeto de autenticação do Spring Security.
     * @return Lista de ProcessoDTO.
     */
    @GetMapping("/processos")
    public ResponseEntity<List<ProcessoDTO>> getMeusProcessos(Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        List<ProcessoDTO> processos = processoService.getProcessosByAdvogadoId(advogado.getId());
        return ResponseEntity.ok(processos);
    }

    /**
     * Endpoint para criar um novo Processo.
     *
     * @param processoDTO      Dados do Processo e status dos documentos.
     * @param clienteId        ID do Cliente ao qual o Processo será associado.
     * @param authentication   Objeto de autenticação do Spring Security.
     * @return ProcessoDTO criado.
     */
    @PostMapping("/processos")
    public ResponseEntity<ProcessoDTO> criarProcesso(@RequestBody ProcessoDTO processoDTO, @RequestParam Long clienteId, Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        try {
            ProcessoDTO savedProcessoDTO = processoService.criarProcesso(processoDTO, advogado.getId(), clienteId);
            return ResponseEntity.ok(savedProcessoDTO);
        } catch (Exception e) {
            System.err.println("Erro ao criar processo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Endpoint para obter um Processo específico.
     *
     * @param id               ID do Processo.
     * @param authentication   Objeto de autenticação do Spring Security.
     * @return ProcessoDTO encontrado.
     */
    @GetMapping("/processos/{id}")
    public ResponseEntity<ProcessoDTO> getProcessoById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        ProcessoDTO processoDTO = processoService.getProcessoById(id, advogado.getId());
        return ResponseEntity.ok(processoDTO);
    }

    /**
     * Endpoint para atualizar um Processo existente.
     *
     * @param id                ID do Processo a ser atualizado.
     * @param processoDTOAtualizado Dados atualizados do Processo e status dos documentos.
     * @param authentication    Objeto de autenticação do Spring Security.
     * @return ProcessoDTO atualizado.
     */
    @PutMapping("/processos/{id}")
    public ResponseEntity<ProcessoDTO> atualizarProcesso(
            @PathVariable Long id,
            @Valid @RequestBody ProcessoDTO processoDTOAtualizado,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        ProcessoDTO atualizado = processoService.atualizarProcesso(id, processoDTOAtualizado, advogado.getId());
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Endpoint para deletar um Processo.
     *
     * @param id               ID do Processo a ser deletado.
     * @param authentication   Objeto de autenticação do Spring Security.
     * @return Resposta vazia com status 204.
     */
    @DeleteMapping("/processos/{id}")
    public ResponseEntity<Void> deletarProcesso(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        processoService.deletarProcesso(id, advogado.getId());
        return ResponseEntity.noContent().build();
    }
}
