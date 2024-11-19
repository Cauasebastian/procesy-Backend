package com.procesy.procesy.controller;

import com.procesy.procesy.security.JwtUtil;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.service.AdvogadoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdvogadoService advogadoService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AdvogadoService advogadoService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.advogadoService = advogadoService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );
            String token = jwtUtil.generateToken(loginRequest.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha na autenticação: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Advogado advogado) {
        if (advogadoService.existsByEmail(advogado.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já está em uso.");
        }
        Advogado savedAdvogado = advogadoService.salvarAdvogado(advogado);
        return ResponseEntity.ok(savedAdvogado);
    }

    // Classes internas para requisições e respostas
    static class LoginRequest {
        private String email;
        private String senha;

        // Getters e Setters
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
