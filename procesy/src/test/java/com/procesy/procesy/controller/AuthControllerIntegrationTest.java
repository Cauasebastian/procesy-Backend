package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.service.AdvogadoService;
import com.procesy.procesy.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AdvogadoService advogadoService;

    @MockBean
    private AuthenticationManager authenticationManager;

    // Teste de Integração para o Login com Sucesso
    @Test
    public void testLoginSuccess() throws Exception {
        String email = "test@domain.com";
        String senha = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(email, senha, new ArrayList<>()));
        when(jwtUtil.generateToken(email)).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    // Teste de Integração para o Registro com Sucesso
    @Test
    public void testRegisterSuccess() throws Exception {
        Advogado advogado = new Advogado();
        advogado.setEmail("new@domain.com");
        advogado.setNome("John Doe");
        advogado.setSenha("password123");

        when(advogadoService.existsByEmail(advogado.getEmail())).thenReturn(false);
        when(advogadoService.salvarAdvogado(any(Advogado.class))).thenReturn(advogado);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"new@domain.com\", \"nome\": \"John Doe\", \"senha\": \"password123\"}") )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@domain.com"))
                .andExpect(jsonPath("$.nome").value("John Doe"));
    }
}