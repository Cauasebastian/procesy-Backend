package com.procesy.procesy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.AdvogadoRepository;
import com.procesy.procesy.repository.ClienteRepository;
import com.procesy.procesy.repository.ProcessoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdvogadoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Advogado advogado;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Limpa o banco de dados
        processoRepository.deleteAll();
        clienteRepository.deleteAll();
        advogadoRepository.deleteAll();

        // Cria dados iniciais
        advogado = new Advogado();
        advogado.setNome("Advogado Teste");
        advogado.setEmail("advogado@example.com");
        advogado.setSenha("senha123");
        advogado = advogadoRepository.save(advogado);

        cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setAdvogado(advogado);
        cliente = clienteRepository.save(cliente);
    }

    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void getMeusClientes_returnsListOfClients() throws Exception {
        mockMvc.perform(get("/advogado/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Cliente Teste")));
    }

    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarCliente_createsAndReturnsClient() throws Exception {
        Cliente newCliente = new Cliente();
        newCliente.setNome("Novo Cliente");
        String json = objectMapper.writeValueAsString(newCliente);

        mockMvc.perform(post("/advogado/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Novo Cliente")));
    }

    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarProcesso_createsAndReturnsProcess() throws Exception {
        Processo newProcesso = new Processo();
        newProcesso.setNumeroProcesso("12345");
        newProcesso.setCliente(cliente);
        newProcesso.setDataInicio(new Date());
        newProcesso.setStatus("Ativo");
        newProcesso.setAcao("Ação de Teste");

        String json = objectMapper.writeValueAsString(newProcesso);

        mockMvc.perform(post("/advogado/processos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .param("clienteId", String.valueOf(cliente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroProcesso", is("12345")));
    }

    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarCliente_whenAdvogadoNotFound_returnsError() throws Exception {
        Cliente newCliente = new Cliente();
        newCliente.setNome("Cliente Inexistente");
        String json = objectMapper.writeValueAsString(newCliente);

        // Força o erro ao deletar o advogado
        advogadoRepository.deleteAll();

        mockMvc.perform(post("/advogado/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}