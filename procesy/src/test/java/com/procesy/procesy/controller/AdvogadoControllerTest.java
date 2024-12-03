package com.procesy.procesy.controller;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMeusClientes_returnsClientList() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setEmail(email);
        List<Cliente> clientes = List.of(new Cliente());

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        when(clienteRepository.findByAdvogado(advogado)).thenReturn(clientes);

        ResponseEntity<List<Cliente>> response = advogadoController.getMeusClientes(authentication);

        assertEquals(ResponseEntity.ok(clientes), response);
    }

    @Test
    void getMeusClientes_advogadoNotFound_throwsException() {
        String email = "advogado@example.com";

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> advogadoController.getMeusClientes(authentication));
    }

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

    @Test
    void criarCliente_advogadoNotFound_throwsException() {
        String email = "advogado@example.com";
        Cliente cliente = new Cliente();

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> advogadoController.criarCliente(cliente, authentication));
    }

    @Test
    void criarProcesso_createsAndReturnsProcess() {
        String email = "advogado@example.com";
        Advogado advogado = new Advogado();
        advogado.setEmail(email);
        Processo processo = new Processo();
        Processo savedProcesso = new Processo();
        Long clienteId = 1L;

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.of(advogado));
        when(processoService.criarProcesso(processo, clienteId, advogado.getId())).thenReturn(savedProcesso);

        ResponseEntity<Processo> response = advogadoController.criarProcesso(processo, clienteId, authentication);

        assertEquals(ResponseEntity.ok(savedProcesso), response);
    }

    @Test
    void criarProcesso_advogadoNotFound_throwsException() {
        String email = "advogado@example.com";
        Processo processo = new Processo();
        Long clienteId = 1L;

        when(authentication.getName()).thenReturn(email);
        when(advogadoRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> advogadoController.criarProcesso(processo, clienteId, authentication));
    }
}