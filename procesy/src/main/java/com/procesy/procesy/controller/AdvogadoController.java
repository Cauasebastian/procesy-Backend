package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.AdvogadoRepository;
import com.procesy.procesy.repository.ClienteRepository;
import com.procesy.procesy.service.ProcessoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/advogado")
public class AdvogadoController {

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

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
    public ResponseEntity<List<Cliente>> getMeusClientes(Authentication authentication) {
        String email = authentication.getName();
        Optional<Advogado> advogadoOpt = advogadoRepository.findByEmail(email);
        if (advogadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        Advogado advogado = advogadoOpt.get();
        List<Cliente> clientes = clienteRepository.findByAdvogado(advogado);
        return ResponseEntity.ok(clientes);
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
     * @return Lista de Processos.
     */
    @GetMapping("/processos")
    public ResponseEntity<List<Processo>> getMeusProcessos(Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        List<Processo> processos = processoService.getProcessosByAdvogadoId(advogado.getId());
        return ResponseEntity.ok(processos);
    }

    /**
     * Endpoint para criar um novo Processo.
     *
     * @param processo        Dados do Processo.
     * @param clienteId       ID do Cliente ao qual o Processo será associado.
     * @param authentication  Objeto de autenticação do Spring Security.
     * @return Processo criado.
     */
    @PostMapping("/processos")
    public ResponseEntity<Processo> criarProcesso(@RequestBody Processo processo, @RequestParam Long clienteId, Authentication authentication) {
        String email = authentication.getName();
        Optional<Advogado> advogadoOpt = advogadoRepository.findByEmail(email);
        if (advogadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        Advogado advogado = advogadoOpt.get();
        try {
            Processo savedProcesso = processoService.criarProcesso(processo, clienteId, advogado.getId());
            return ResponseEntity.ok(savedProcesso);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Endpoint para obter um Processo específico.
     *
     * @param id               ID do Processo.
     * @param authentication   Objeto de autenticação do Spring Security.
     * @return Processo encontrado.
     */
    @GetMapping("/processos/{id}")
    public ResponseEntity<Processo> getProcessoById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        Processo processo = processoService.getProcessoById(id, advogado.getId());
        return ResponseEntity.ok(processo);
    }

    /**
     * Endpoint para atualizar um Processo existente.
     *
     * @param id                ID do Processo a ser atualizado.
     * @param processoAtualizado Dados atualizados do Processo.
     * @param authentication    Objeto de autenticação do Spring Security.
     * @return Processo atualizado.
     */
    @PutMapping("/processos/{id}")
    public ResponseEntity<Processo> atualizarProcesso(
            @PathVariable Long id,
            @Valid @RequestBody Processo processoAtualizado,
            Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

        Processo processo = processoService.atualizarProcesso(id, processoAtualizado, advogado.getId());
        return ResponseEntity.ok(processo);
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
