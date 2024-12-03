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

/**
 * Testes de integração para a classe AdvogadoController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdvogadoControllerTesteDeIntegracao {

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

    /**
     * Configura o ambiente de teste antes de cada teste.
     * Limpa o banco de dados e cria dados iniciais.
     */
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

    /**
     * Testa o método getMeusClientes para garantir que retorna uma lista de clientes para o advogado autenticado.
     */
    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void getMeusClientes_retornarListaDeClientes() throws Exception {
        mockMvc.perform(get("/advogado/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Cliente Teste")));
    }

    /**
     * Testa o método criarCliente para garantir que cria e retorna um novo cliente.
     */
    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarCliente_criaERetornaCliente() throws Exception {
        Cliente novoCliente = new Cliente();
        novoCliente.setNome("Novo Cliente");
        String json = objectMapper.writeValueAsString(novoCliente);

        mockMvc.perform(post("/advogado/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Novo Cliente")));
    }

    /**
     * Testa o método criarProcesso para garantir que cria e retorna um novo processo associado a um cliente e advogado.
     */
    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarProcesso_criaERetornaProcesso() throws Exception {
        Processo novoProcesso = new Processo();
        novoProcesso.setNumeroProcesso("12345");
        novoProcesso.setCliente(cliente);
        novoProcesso.setDataInicio(new Date());
        novoProcesso.setStatus("Ativo");
        novoProcesso.setAcao("Ação de Teste");
        String json = objectMapper.writeValueAsString(novoProcesso);
        mockMvc.perform(post("/advogado/processos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .param("clienteId", String.valueOf(cliente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroProcesso", is("12345")));
    }

    /**
     * Testa o método criarCliente para garantir que retorna um erro quando o advogado não for encontrado.
     */
    @Test
    @WithMockUser(username = "advogado@example.com", roles = "ADVOGADO")
    void criarCliente_quandoAdvogadoNaoEncontrado_retornarErro() throws Exception {
        Cliente novoCliente = new Cliente();
        novoCliente.setNome("Cliente Inexistente");
        String json = objectMapper.writeValueAsString(novoCliente);

        // Força o erro ao deletar o advogado
        advogadoRepository.deleteAll();

        mockMvc.perform(post("/advogado/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}