package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.service.AdvogadoService;
import com.procesy.procesy.service.OpenAIAssistantService;
import com.procesy.procesy.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final OpenAIAssistantService assistantService;
    private final JwtUtil jwtUtil;
    private final AdvogadoService advogadoService;

    public AssistantController(OpenAIAssistantService assistantService,
                               JwtUtil jwtUtil,
                               AdvogadoService advogadoService) {
        this.assistantService = assistantService;
        this.jwtUtil = jwtUtil;
        this.advogadoService = advogadoService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(
            @RequestBody String pergunta,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            // Validação do token
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Token inválido ou ausente");
            }

            String token = authorizationHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body("Token inválido ou expirado");
            }

            // Obter email do token
            String email = jwtUtil.getUsernameFromJWT(token);

            // Buscar advogado pelo email
            Advogado advogado = advogadoService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

            // Verificar assistantId
            String assistantId = advogado.getAssistantId();
            if (assistantId == null || assistantId.isEmpty()) {
                return ResponseEntity.status(400).body("ID do assistente não configurado");
            }

            // Chamar o serviço
            String resposta = assistantService.askAssistant(pergunta, assistantId);
            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }
}