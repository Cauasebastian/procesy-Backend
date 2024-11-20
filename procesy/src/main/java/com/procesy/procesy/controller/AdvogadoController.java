package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.repository.AdvogadoRepository;
import com.procesy.procesy.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/advogados")
public class AdvogadoController {

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Exemplo de endpoint para obter os Clientes do Advogado autenticado
    @GetMapping("/meus-clientes")
    public ResponseEntity<List<Cliente>> getMeusClientes(Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        List<Cliente> clientes = clienteRepository.findByAdvogado(advogado);
        return ResponseEntity.ok(clientes);
    }

    // Criar um novo Cliente
    @PostMapping("/clientes")
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente, Authentication authentication) {
        String email = authentication.getName();
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        cliente.setAdvogado(advogado);
        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.ok(salvo);
    }

    // Atualizar um Cliente existente
    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> atualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteAtualizado, Authentication authentication) {
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

    // Deletar um Cliente
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id, Authentication authentication) {
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
}
