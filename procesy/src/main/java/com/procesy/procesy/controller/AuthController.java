package com.procesy.procesy.controller;

import com.procesy.procesy.security.JwtUtil;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.service.advogado.AdvogadoService;
import com.procesy.procesy.service.OpenAIAssistantService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdvogadoService advogadoService;
    private final OpenAIAssistantService openAIAssistantService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AdvogadoService advogadoService, OpenAIAssistantService openAIAssistantService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.advogadoService = advogadoService;
        this.openAIAssistantService = openAIAssistantService;
    }
    //HELLO
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
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
            Advogado advogado = advogadoService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Advogado não encontrado"));

            String token = jwtUtil.generateToken(loginRequest.getEmail(), "ADVOGADO", null);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falha na autenticação: " + e.getMessage());
        }
    }
    private static final ExecutorService keyGenExecutor = Executors.newFixedThreadPool(4);
    private static KeyPairGenerator cachedKeyGen;

    static {
        try {
            cachedKeyGen = KeyPairGenerator.getInstance("RSA");
            cachedKeyGen.initialize(2048); // Reduza para 2048 para testes
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize KeyPairGenerator", e);
        }
    }

    @PostMapping("/register")
    @Transactional
    public CompletableFuture<ResponseEntity<?>> registerAsync(@Valid @RequestBody Advogado advogado, BindingResult result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (advogadoService.existsByEmail(advogado.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já está em uso.");
                }

                // Geração assíncrona de chaves
                KeyPair keyPair = generateRSAKeyPairAsync(2048).get();

                byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
                advogado.setPublicKey(publicKeyBytes);

                // Dentro do método registerAsync de AuthController, logo após salvar o Advogado:
                Advogado savedAdvogado = advogadoService.salvarAdvogado(advogado);

                // Obter (ou criar) o vector store para esse advogado
                String vectorStoreId = openAIAssistantService.getOrCreateVectorStore(advogado.getNome());
                savedAdvogado.setVectorStoreId(vectorStoreId);

// Criação do Assistant exclusivo para o advogado
                String assistantId = openAIAssistantService.createAssistant(advogado.getNome(), vectorStoreId);
                savedAdvogado.setAssistantId(assistantId);


// Atualizar o advogado com os novos IDs (caso seu serviço atualize o registro)
                advogadoService.atualizarAdvogado(savedAdvogado);

                String privateKeyBase64 = Base64.getEncoder().encodeToString(
                        keyPair.getPrivate().getEncoded()
                );

                return ResponseEntity.ok(
                        new AdvogadoRegistrationResponse(savedAdvogado, privateKeyBase64)
                );
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro no registro: " + e.getMessage());
            }
        }, keyGenExecutor);
    }

    private static CompletableFuture<KeyPair> generateRSAKeyPairAsync(int keySize) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (cachedKeyGen) {
                if (cachedKeyGen.getAlgorithm().equals("RSA")) {
                    cachedKeyGen.initialize(keySize);
                }
                return cachedKeyGen.generateKeyPair();
            }
        }, keyGenExecutor);
    }

    // Classe DTO para resposta de registro
    static class AdvogadoRegistrationResponse {
        private Long id;
        private String nome;
        private String email;
        private String privateKey; // Em Base64

        public AdvogadoRegistrationResponse(Advogado advogado, String privateKey) {
            this.id = advogado.getId();
            this.nome = advogado.getNome();
            this.email = advogado.getEmail();
            this.privateKey = privateKey;
        }

        // Getters
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getPrivateKey() { return privateKey; }
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
