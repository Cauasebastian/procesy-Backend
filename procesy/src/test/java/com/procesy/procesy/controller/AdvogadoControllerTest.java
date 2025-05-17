package com.procesy.procesy.controller;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.AdvogadoRepository;
import com.procesy.procesy.repository.ClienteRepository;
import com.procesy.procesy.service.ProcessoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AdvogadoController class.
 */
class AdvogadoControllerTest {

    @Mock
    private AdvogadoRepository advogadoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProcessoService processoService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdvogadoController advogadoController;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Testa o método getMeusClientes para garantir que ele retorna a lista de clientes do advogado autenticado.
     */
    @Test
    void getMeusClientes_returnsClientList() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setEmail(email);
        List<Cliente> clientes = List.of(new Cliente());

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        when(clienteRepository.findByAdvogado(advogado)).thenReturn(clientes);

        ResponseEntity<List<ClienteDTO>> response = advogadoController.getMeusClientes(authentication);

        assertEquals(ResponseEntity.ok(clientes), response);
    }

    /**
     * Testa o método getMeusClientes para garantir que lança exceção quando o advogado não é encontrado.
     */
    @Test
    void getMeusClientes_advogadoNotFound_throwsException() {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setAdvogado(null);

        when(clienteRepository.findById(clienteId)).thenReturn(java.util.Optional.of(cliente));

        assertThrows(RuntimeException.class, () -> {
            advogadoController.atualizarCliente(clienteId, cliente, null);
        });
    }

    /**
     * Testa o método criarCliente para garantir que cria e retorna um novo cliente.
     */
    @Test
    void criarCliente_createsAndReturnsClient() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setEmail(email);
        Cliente cliente = new Cliente();
        Cliente savedCliente = new Cliente();

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        when(clienteRepository.save(cliente)).thenReturn(savedCliente);

        ResponseEntity<Cliente> response = advogadoController.criarCliente(cliente, authentication);

        assertEquals(ResponseEntity.ok(savedCliente), response);
    }
    /**
     * Testa o método atualizarCliente para garantir que atualiza e retorna o cliente atualizado.
     */

    @Test
    void atualizarCliente_updatesAndReturnsClient() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setId(1L);
        advogado.setEmail(email);
        Cliente cliente = new Cliente();
        //UUID
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Cliente Teste");
        cliente.setAdvogado(advogado);
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("Cliente Atualizado");

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(clienteAtualizado);

        ResponseEntity<Cliente> response = advogadoController.atualizarCliente(cliente.getId(), clienteAtualizado, authentication);

        assertEquals(ResponseEntity.ok(clienteAtualizado), response);
    }

    /**
     * Testa o método criarCliente para garantir que lança 404 quando o advogado não é encontrado.
     */
    @Test
    void criarCliente_advogadoNaoEncontrado_retornaErro404() {
        String email = "advogado@example.com";
        Cliente cliente = new Cliente();

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<Cliente> response = advogadoController.criarCliente(cliente, authentication);

        // Verifica se o status retornado é 404 (Not Found)
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody()); // Verifica que o corpo da resposta é nulo
    }


    /**
     * Testa o método deletarProcesso para garantir que deleta o processo corretamente.
     */
    @Test
    void deletarProcesso_deletesProcess() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setEmail(email);
        Processo processo = new Processo();
        processo.setId(1L);

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        doNothing().when(processoService).deletarProcesso(processo.getId(), advogado.getId());

        ResponseEntity<Void> response = advogadoController.deletarProcesso(processo.getId(), authentication);

        assertEquals(ResponseEntity.noContent().build(), response);
        verify(processoService, times(1)).deletarProcesso(processo.getId(), advogado.getId());
    }

}