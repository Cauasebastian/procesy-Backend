package com.procesy.procesy.controller;

import com.procesy.procesy.security.JwtUtil;
import com.procesy.procesy.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/cliente")
public class AuthClientController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ClienteService clienteService;

    public AuthClientController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ClienteService clienteService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.clienteService = clienteService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );
            String token = jwtUtil.generateToken(loginRequest.getEmail(), "CLIENTE");
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha na autenticação: " + e.getMessage());
        }
    }
    @PostMapping("/first-register")
    public ResponseEntity<?> register(@Valid @RequestBody registerRequest registerRequest) {
        try {
            clienteService.register(registerRequest.getEmail(), registerRequest.getSenha());
            return ResponseEntity.status(HttpStatus.CREATED).body("Cliente registrado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falha ao registrar cliente: " + e.getMessage());
        }
    }
    //HELLO authenticado com o token
    @GetMapping("/hello")
    public ResponseEntity<String> hello(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok("Hello, " + email + "!");
    }

    static class registerRequest {
        private String email;
        private String senha;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    // Classes DTO para requisição e resposta (pode ser movido para um pacote comum)
    static class LoginRequest {
        private String email;
        private String senha;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    static class AuthResponse {
        private String token;

        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
    }
}