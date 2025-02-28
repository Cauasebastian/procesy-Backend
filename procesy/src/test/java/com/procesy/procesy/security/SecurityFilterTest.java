package com.procesy.procesy.security;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SecurityFilterTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            testRegistrationRateLimit();
            testLoginRateLimit();
            testDDoSProtection();
        } catch (Exception e) {
            System.err.println("Erro nos testes: " + e.getMessage());
        }
    }

    private static void testRegistrationRateLimit() {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        int totalRequests = 22;
        for (int i = 0; i < totalRequests; i++) {
            final int count = i;
            try {
                String email = "user" + count + UUID.randomUUID() + "@example.com";
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/auth/register"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(
                                        String.format("{\"nome\": \"User %d\", \"email\": \"%s\", \"senha\": \"senha123\"}", count, email)
                                ))
                                .timeout(Duration.ofSeconds(15))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                switch (response.statusCode()) {
                    case 200 -> success.incrementAndGet();
                    case 429 -> blocked.incrementAndGet();
                    default -> errors.incrementAndGet();
                }
                System.out.println("Registro " + count + " status: " + response.statusCode());
            } catch (Exception e) {
                errors.incrementAndGet();
                System.err.println("Erro no registro " + count + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== Teste Registro ===");
        System.out.println("Sucessos: " + success.get() + " (<=20 esperado)");
        System.out.println("Bloqueios: " + blocked.get() + " (>=2 esperado)");
        System.out.println("Erros: " + errors.get());
    }

    private static void testLoginRateLimit() {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        // 1. Usar o MESMO email e senha do registro
        String email = "teste"  + "@example.com";
        String senha = "senha123"; // Senha deve ser igual no registro e login

        // 2. Esperar registro completar

        int totalRequests = 12;
        for (int i = 0; i < totalRequests; i++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/auth/login"))
                                .header("Content-Type", "application/json")
                                // 3. Usar a senha correta
                                .POST(HttpRequest.BodyPublishers.ofString(
                                        String.format("{\"email\": \"%s\", \"senha\": \"%s\"}", email, senha)
                                ))
                                .timeout(Duration.ofSeconds(15))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                // 4. Tratar status 401 como erro de autenticação
                if (response.statusCode() == 200) {
                    success.incrementAndGet();
                } else if (response.statusCode() == 429) {
                    blocked.incrementAndGet();
                } else {
                    errors.incrementAndGet();
                    System.err.println("Erro no login " + i + ": " + response.body());
                }

                System.out.println("Login " + i + " status: " + response.statusCode());
            } catch (Exception e) {
                errors.incrementAndGet();
                System.err.println("Erro no login " + i + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== Teste Login ===");
        System.out.println("Sucesso (<=10 esperado): " + success.get());
        System.out.println("Bloqueado (>=2 esperado): " + blocked.get());
        System.out.println("Erros: " + errors.get());
    }

    private static void testDDoSProtection() {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        int totalRequests = 105;
        int otherTestsRequests = 12 + 22; // Login (12) + Register (22)
        int ddosRequests = totalRequests - otherTestsRequests;
        for (int i = 0; i < ddosRequests; i++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/auth/hello"))
                                .timeout(Duration.ofSeconds(15))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                switch (response.statusCode()) {
                    case 200 -> success.incrementAndGet();
                    case 429 -> blocked.incrementAndGet();
                    default -> errors.incrementAndGet();
                }
                System.out.println("DDoS " + i + " status: " + response.statusCode());
            } catch (Exception e) {
                errors.incrementAndGet();
                System.err.println("Erro no DDoS " + i + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== Teste DDoS (Ajustado) ===");
        System.out.println("Requisições DDoS: " + ddosRequests);
        System.out.println("Sucesso (<= " + (100 - otherTestsRequests) + " esperado): " + success.get());
        System.out.println("Bloqueado (>= " + (ddosRequests - (100 - otherTestsRequests)) + " esperado): " + blocked.get());
        System.out.println("Erros: " + errors.get());
    }

    private static void criarUsuarioTeste(String email, String senha) {
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/auth/register"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    String.format("{\"nome\": \"Test User\", \"email\": \"%s\", \"senha\": \"%s\"}", email, senha)
                            ))
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            System.out.println("Criou usuário teste: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Erro ao criar usuário: " + e.getMessage());
        }
    }
}