package com.procesy.procesy.service;

// package com.procesy.procesy.service;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class OpenAIAssistantService {

    private static final String API_KEY = ""; // A chave de api vai aqui
    private static final String BASE_URL = "https://api.openai.com/v1";
    // Remova o ASSISTANT_ID fixo, pois cada advogado terá o seu

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("OpenAI-Beta", "assistants=v2");
        headers.set("OpenAI-Project", "proj_nf5CPRkzaN7P538YQvB2l4sX");
        return headers;
    }

    // Método para criar um novo Assistant para o advogado
    public String createAssistant(String advogadoNome, String vectorStoreId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Assistente de " + advogadoNome);
        requestBody.put("model", "gpt-3.5-turbo"); // Modelo atualizado
        requestBody.put("instructions", "Você é um assistente jurídico inteligente integrado ao Procesy, um software especializado em gerenciamento de processos jurídicos para advogados. Sua missão é facilitar a rotina dos usuários respondendo dúvidas, organizando tarefas, monitorando prazos, auxiliando na gestão de documentos e sugerindo boas práticas jurídicas.\n" +
                "\n" +
                "Contexto:\n" +
                "\n" +
                "O usuário é um advogado, estagiário de direito ou membro de um escritório jurídico.\n" +
                "\n" +
                "Você deve sempre usar uma linguagem formal, clara e objetiva, adaptando-se ao nível técnico do usuário.\n" +
                "\n" +
                "Sempre priorize a precisão, prazos legais e ética profissional.\n" +
                "\n" +
                "O sistema possui informações sobre processos, clientes, documentos, prazos e compromissos da agenda jurídica.\n" +
                "\n" +
                "Suas habilidades incluem:\n" +
                "\n" +
                "Informar sobre o andamento de processos judiciais cadastrados.\n" +
                "\n" +
                "Gerar minutas de petições, contratos ou relatórios conforme o tipo de processo.\n" +
                "\n" +
                "Lembrar o usuário de prazos, audiências e tarefas pendentes.\n" +
                "\n" +
                "Sugerir modelos de documentos ou boas práticas para gestão jurídica.\n" +
                "\n" +
                "Explicar termos jurídicos ou procedimentos legais de forma acessível.\n" +
                "\n" +
                "Restrições:\n" +
                "\n" +
                "Nunca ofereça aconselhamento jurídico pessoal — apenas informações e sugestões gerais com base nos dados fornecidos.\n" +
                "\n" +
                "Quando não tiver certeza de algo, sugira ao usuário consultar um especialista ou verificar fontes oficiais.");

        // Configuração das ferramentas
        List<Map<String, String>> tools = new ArrayList<>();
        Map<String, String> tool = new HashMap<>();
        tool.put("type", "file_search");
        tools.add(tool);
        requestBody.put("tools", tools);

        // Configuração dos recursos da ferramenta
        Map<String, Object> toolResources = new HashMap<>();
        Map<String, Object> fileSearchResource = new HashMap<>();
        fileSearchResource.put("vector_store_ids", Collections.singletonList(vectorStoreId));
        toolResources.put("file_search", fileSearchResource);
        requestBody.put("tool_resources", toolResources);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, getHeaders());
        Map<String, Object> response = restTemplate.postForObject(BASE_URL + "/assistants", entity, Map.class);

        return (String) response.get("id");
    }
    // Método para perguntar ao assistente
    public String askAssistant(String question, String assistantId) throws InterruptedException {
        // 1 - Criar thread
        Map<String, Object> threadBody = new HashMap<>();
        HttpEntity<Map<String, Object>> threadRequest = new HttpEntity<>(threadBody, getHeaders());
        Map<String, Object> threadResponse = restTemplate.postForObject(BASE_URL + "/threads", threadRequest, Map.class);
        String threadId = (String) threadResponse.get("id");

        // 2 - Adicionar mensagem SEM anexar arquivos (já está no vector store)
        Map<String, Object> messageBody = Map.of(
                "role", "user",
                "content", question
        );
        HttpEntity<Map<String, Object>> messageRequest = new HttpEntity<>(messageBody, getHeaders());
        restTemplate.postForObject(BASE_URL + "/threads/" + threadId + "/messages", messageRequest, Map.class);

        // 3 - Iniciar run sem especificar tools novamente (herda do assistant)
        Map<String, Object> runBody = new HashMap<>();
        runBody.put("assistant_id", assistantId);

        // Configuração adicional para incluir resultados da busca
        runBody.put("additional_instructions", "Use apenas documentos do repositório jurídico cadastrado.");

        HttpEntity<Map<String, Object>> runRequest = new HttpEntity<>(runBody, getHeaders());
        Map<String, Object> runResponse = restTemplate.postForObject(BASE_URL + "/threads/" + threadId + "/runs", runRequest, Map.class);
        String runId = (String) runResponse.get("id");

        // 4 - Aguardar processamento com timeout maior
        String status = "queued";
        int attempts = 0;
        while (!status.equals("completed") && attempts < 30) { // Timeout de 30 segundos
            Thread.sleep(1000);
            ResponseEntity<Map> runStatusResponse = restTemplate.exchange(
                    BASE_URL + "/threads/" + threadId + "/runs/" + runId,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders()),
                    Map.class
            );
            status = (String) runStatusResponse.getBody().get("status");
            if (status.equals("failed") || status.equals("cancelled")) {
                return "Erro na consulta: " + status;
            }
            attempts++;
        }

        // 5 - Processar resposta com citações melhoradas
        ResponseEntity<Map> messagesResponse = restTemplate.exchange(
                BASE_URL + "/threads/" + threadId + "/messages?order=asc",
                HttpMethod.GET,
                new HttpEntity<>(getHeaders()),
                Map.class
        );

        return processMessages((List<Map<String, Object>>) messagesResponse.getBody().get("data"));
    }

    // Novo método para processar as citações
    // Método para processar as citações corrigido
    private String processMessages(List<Map<String, Object>> messages) {
        Map<String, Object> lastMessage = messages.get(messages.size() - 1);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) lastMessage.get("content");

        StringBuilder response = new StringBuilder();
        Set<String> citedFiles = new LinkedHashSet<>();
        Map<String, String> fileCitations = new HashMap<>();

        // Processar conteúdo principal
        for (Map<String, Object> content : contentList) {
            if ("text".equals(content.get("type"))) {
                Map<String, Object> textContent = (Map<String, Object>) content.get("text");
                String text = (String) textContent.get("value");

                // Processar citações e criar marcadores
                if (textContent.get("annotations") != null) {
                    int citationIndex = 1;
                    for (Map<String, Object> annotation : (List<Map<String, Object>>) textContent.get("annotations")) {
                        if ("file_citation".equals(annotation.get("type"))) {
                            Map<String, Object> citation = (Map<String, Object>) annotation.get("file_citation");
                            String fileId = (String) citation.get("file_id");
                            String fileName = getFileName(fileId);

                            if (fileName == null) fileName = "Documento de Referência";

                            String marker = "[" + citationIndex + "]";
                            text = text.replace((String) annotation.get("text"), marker);
                            fileCitations.put(marker, fileName);
                            citedFiles.add(fileName);
                            citationIndex++;
                        }
                    }
                }
                response.append(text);
            }
        }

        // Adicionar referências formatadas
        if (!citedFiles.isEmpty()) {
            response.append("\n\n──────────────────────────────");
            response.append("\nREFERÊNCIAS DOCUMENTAIS:\n");

            int refIndex = 1;
            for (String fileName : citedFiles) {
                response.append(refIndex).append(". ").append(fileName).append("\n");
                refIndex++;
            }
        }

        return response.toString();
    }

    // Novo método para obter nome do arquivo
    private String getFileName(String fileId) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/files/" + fileId,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders()),
                    Map.class
            );

            return (String) response.getBody().get("filename");
        } catch (Exception e) {
            return null;
        }
    }

    // Já existe o método get_or_create_vector_store(String clientName) no seu script original,
    // que você pode reutilizar para associar o vector store ao advogado (pelo nome)
    public String getOrCreateVectorStore(String clientName) {
        String vsName = clientName + "_assets";
        // Use HttpEntity para incluir os headers
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/vector_stores?limit=100",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> vectorStores = response.getBody();
            // Supondo que vectorStores contenha um campo "data" com a lista de vector stores
            List<Map<String, Object>> data = (List<Map<String, Object>>) vectorStores.get("data");
            for (Map<String, Object> vs : data) {
                if (vs.get("name").equals(vsName)) {
                    return (String) vs.get("id");
                }
            }
        }

        // Se não existir, criar
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", vsName);
        requestBody.put("expires_after", Map.of("anchor", "last_active_at", "days", 30));

        HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(requestBody, getHeaders());
        Map<String, Object> createResponse = restTemplate.postForObject(BASE_URL + "/vector_stores", createEntity, Map.class);
        return (String) createResponse.get("id");
    }
}