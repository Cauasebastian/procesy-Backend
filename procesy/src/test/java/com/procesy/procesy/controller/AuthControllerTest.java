package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.security.JwtUtil;
import com.procesy.procesy.service.advogado.AdvogadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AdvogadoService advogadoService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // Teste de Login com Sucesso
    @Test
    public void testLoginSuccess() throws Exception {
        String email = "test@domain.com";
        String senha = "password123";

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setSenha(senha);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(email)).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    // Teste de Login com Falha de Autenticação
    @Test
    public void testLoginFailure() throws Exception {
        String email = "wrong@domain.com";
        String senha = "wrongpassword";

        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Falha na autenticação") {});

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falha na autenticação: Falha na autenticação"));
    }


    // Teste de Registro com Sucesso
    @Test
    public void testRegisterSuccess() throws Exception {
        String email = "new@domain.com";
        String nome = "John Doe";
        String senha = "password123"; // Definindo a senha corretamente

        when(advogadoService.existsByEmail(email)).thenReturn(false);
        Advogado advogado = new Advogado();
        advogado.setId(1L);
        advogado.setNome(nome);
        advogado.setEmail(email);
        advogado.setSenha(senha);
        when(advogadoService.salvarAdvogado(any(Advogado.class))).thenReturn(advogado);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"nome\": \"" + nome + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isOk())  // Espera sucesso
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nome").value(nome));
    }


    // Teste de Registro com Conflito de E-mail
    @Test
    public void testRegisterEmailConflict() throws Exception {
        Advogado advogado = new Advogado();
        advogado.setEmail("existing@domain.com");
        advogado.setNome("Jane Doe");

        when(advogadoService.existsByEmail(advogado.getEmail())).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"existing@domain.com\", \"nome\": \"Jane Doe\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email já está em uso."));
    }
    // Teste de Registro com Conflito de E-mail
    @Test
    public void testRegisterInvalidEmail() throws Exception {
        String email = "invalid-email"; // E-mail inválido

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"nome\": \"John Doe\"}"))
                .andExpect(status().isBadRequest()) // Espera erro de solicitação malformada
                .andExpect(content().string("A senha é obrigatória"));
    }

    // Teste de Senha Inválida no Login
    @Test
    public void testLoginInvalidPassword() throws Exception {
        String email = "test@domain.com";
        String senha = "wrongpassword"; // Senha incorreta

        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Senha incorreta") {});

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falha na autenticação: Senha incorreta"));
    }


    // Teste de Registro com Dados Incompletos (Campos Opcionais)
    @Test
    public void testRegisterMissingFields() throws Exception {
        // O campo "nome" é obrigatório, mas "nome" está ausente
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"missingName@domain.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A senha é obrigatória"));
    }

}
