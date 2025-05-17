package com.procesy.procesy.controller;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.service.advogado.AdvogadoService;
import com.procesy.procesy.service.OpenAIAssistantService;
import com.procesy.procesy.security.JwtUtil;
import com.procesy.procesy.service.cliente.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final OpenAIAssistantService assistantService;
    private final JwtUtil jwtUtil;
    private final AdvogadoService advogadoService;
    private final ClienteService clienteService;

    public AssistantController(OpenAIAssistantService assistantService,
                               JwtUtil jwtUtil,
                               AdvogadoService advogadoService,
                               ClienteService clienteService) {
        this.assistantService = assistantService;
        this.clienteService = clienteService;
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

            // Obter informações do token
            String role = jwtUtil.getRoleFromJWT(token);
            String resposta;

            if ("ADVOGADO".equalsIgnoreCase(role)) {
                // Processamento para advogados
                String email = jwtUtil.getUsernameFromJWT(token);
                Advogado advogado = advogadoService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));

                resposta = processarAdvogado(pergunta, advogado);
            } else if ("CLIENTE".equalsIgnoreCase(role)) {
                // Processamento para clientes
                String clientId = jwtUtil.getClientIdFromJWT(token).toString();
                resposta = processarCliente(pergunta, clientId);
            } else {
                return ResponseEntity.status(403).body("Acesso negado");
            }

            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    private String processarAdvogado(String pergunta, Advogado advogado) throws Exception {
        if (advogado.getAssistantId() == null || advogado.getAssistantId().isEmpty()) {
            throw new RuntimeException("ID do assistente não configurado");
        }
        return assistantService.askAssistant(pergunta, advogado.getAssistantId(),null);
    }

    private String processarCliente(String pergunta, String clientId) throws Exception {
        Cliente cliente = clienteService.findById(UUID.fromString(clientId));

        Advogado advogado = cliente.getAdvogado();
        if (advogado == null) {
            throw new RuntimeException("Cliente não possui advogado associado");
        }

        String vectorStoreId = assistantService.getOrCreateVectorStore(advogado.getNome());
        return assistantService.askAssistant(
                pergunta,
                advogado.getAssistantId(),
                clientId
        );
    }
}