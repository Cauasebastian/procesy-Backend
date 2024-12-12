package com.procesy.procesy.controller;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.dto.ProcessoDTO;
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
import java.util.stream.Collectors;

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

    // ... [Endereços de Clientes permanecem inalterados]

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
