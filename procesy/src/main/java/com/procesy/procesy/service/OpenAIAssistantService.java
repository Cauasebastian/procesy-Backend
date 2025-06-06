package com.procesy.procesy.service;

// package com.procesy.procesy.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIAssistantService {

    // Chave de API e URL base do OpenAI por meio da OPENAI_API_KEY
    @Value("${openai.api.key}")
    private String API_KEY;

    private static final String BASE_URL = "https://api.openai.com/v1";
    // Remova o ASSISTANT_ID fixo, pois cada advogado terá o seu

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CacheEntry> threadCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        String threadId;
        long createdAt;

        CacheEntry(String threadId) {
            this.threadId = threadId;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TimeUnit.DAYS.toMillis(2);
        }
    }

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
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("instructions",
                "Você é um assistente jurídico inteligente integrado ao Procesy, um software especializado em gerenciamento de processos jurídicos para advogados. " +
                        "Sua missão é facilitar a rotina dos usuários respondendo dúvidas, organizando tarefas, monitorando prazos, auxiliando na gestão de documentos e sugerindo boas práticas jurídicas.\n\n" +

                        "Contexto:\n" +
                        "O usuário é um advogado, estagiário de direito ou membro de um escritório jurídico.\n" +
                        "Você deve sempre usar uma linguagem formal, clara e objetiva, adaptando-se ao nível técnico do usuário.\n" +
                        "Sempre priorize a precisão, prazos legais e ética profissional.\n" +
                        "O sistema possui informações sobre processos, clientes, documentos, prazos e compromissos da agenda jurídica.\n\n" +

                        "Suas habilidades incluem:\n" +
                        "- Informar sobre o andamento de processos judiciais cadastrados.\n" +
                        "- Gerar minutas de petições, contratos ou relatórios conforme o tipo de processo.\n" +
                        "- Lembrar o usuário de prazos, audiências e tarefas pendentes.\n" +
                        "- Sugerir modelos de documentos ou boas práticas para gestão jurídica.\n" +
                        "- Explicar termos jurídicos ou procedimentos legais de forma acessível.\n\n" +

                        "Restrições:\n" +
                        "- Nunca ofereça aconselhamento jurídico pessoal — apenas informações e sugestões gerais com base nos dados fornecidos.\n" +
                        "- Quando não tiver certeza de algo, sugira ao usuário consultar um especialista ou verificar fontes oficiais.\n\n" +

                        "3. Formato esperado para os arquivos: [ID Processo]_[CLIENT_ID]_[Nome Arquivo]\n" +
                        "   3.1. Exemplo de nome correto: '1_12345_1234567890_nome_do_arquivo.pdf'\n" +
                        "  Se o client_ID for **nulo**, isso indica que o usuário é um advogado e ele **tem acesso a todos os arquivos**, sem considerar o nome do arquivo\n" +
                        "   3.2. Se o CLIENT_ID no nome do arquivo não corresponder ao ID fornecido, **não use** esse arquivo\n" +
                        "   3.3. Se o arquivo **não contiver** o CLIENT_ID no nome, **não use** esse arquivo\n" +
                        "4. Se o client_ID for **nulo**, isso indica que o usuário é um advogado e ele **tem acesso a todos os arquivos**, sem considerar o nome do arquivo\n" +
                        "5. Se não houver nenhum arquivo com o CLIENT_ID especificado, informe: 'Nenhum documento encontrado para este cliente.'\n" +
                        "6. Jamais use arquivos de outros clientes, mesmo que o nome do arquivo tenha o formato correto, a menos que o CLIENT_ID corresponda exatamente ao ID informado\n" +
                        "7. Caso o CLIENT_ID seja nulo (advogado), o advogado tem **acesso a todos os arquivos**, independentemente do nome ou do CLIENT_ID.\n"
        );


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
    public String askAssistant(String question, String assistantId, String clientId)
            throws InterruptedException {
        // 1 - Obter ou criar thread do cache
        String threadId = getOrCreateThreadId(assistantId);


        // Adicione contexto invisível ao usuário
        String hiddenContext = "DIRETRIZES ABSOLUTAS:\n" +
                "1. CLIENT_ID: " + clientId + "\n" +
                "2. Use EXCLUSIVAMENTE arquivos que contenham o seguinte ID no nome: '" + clientId + "'\n" +
                "3. Formato esperado para o nome do arquivo: [ID Processo]_[CLIENT_ID]_[Nome Arquivo]\n" +
                "3.1. Exemplo de nome correto: '1_12345_1234567890_nome_do_arquivo.pdf'\n" +
                "3.2. Se o client_ID no nome do arquivo não corresponder ao ID fornecido, **não utilize** o arquivo\n" +
                "3.3. Se o client_ID for **nulo**, isso indica que o usuário é um advogado e ele **tem acesso a todos os arquivos**, sem considerar o nome do arquivo\n" +
                "4. Se não houver nenhum arquivo correspondente ao client_ID ou não houver arquivos, informe: 'Nenhum documento encontrado para este cliente'\n" +
                "5. Jamais use arquivos de outros clientes, mesmo que o nome do arquivo tenha o formato correto, a menos que o client_ID corresponda ao ID do cliente informado\n" +
                "6. Caso o client_ID seja nulo, o advogado pode acessar **qualquer arquivo**, independentemente do nome\n" +
                "7. Em casos de exceção, como erros de validação, o sistema deve gerar um log detalhado para ajudar na depuração.\n" +
                "8. Certifique-se de que **somente arquivos que correspondem exatamente ao client_ID** sejam retornados para o cliente, exceto no caso de advogados.\n";



        Map<String, Object> messageBody = Map.of(
                "role", "user",
                "content",hiddenContext + "\n" + question
        );

        HttpEntity<Map<String, Object>> messageRequest = new HttpEntity<>(messageBody, getHeaders());
        restTemplate.postForObject(BASE_URL + "/threads/" + threadId + "/messages", messageRequest, Map.class);

        // 3 - Iniciar run (restante do método mantido)
        Map<String, Object> runBody = new HashMap<>();
        runBody.put("assistant_id", assistantId);

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

    // Novo método auxiliar para gerenciar o cache
    private String getOrCreateThreadId(String assistantId) {
        CacheEntry entry = threadCache.get(assistantId);

        if (entry != null && !entry.isExpired()) {
            return entry.threadId;
        }

        // Criar nova thread se expirada ou inexistente
        Map<String, Object> threadBody = new HashMap<>();
        HttpEntity<Map<String, Object>> threadRequest = new HttpEntity<>(threadBody, getHeaders());
        Map<String, Object> threadResponse = restTemplate.postForObject(BASE_URL + "/threads", threadRequest, Map.class);
        String newThreadId = (String) threadResponse.get("id");

        threadCache.put(assistantId, new CacheEntry(newThreadId));
        return newThreadId;
    }

    // Método de limpeza agendada
    @Scheduled(fixedRate = 3600000) // Executa a cada hora
    public void cleanupExpiredThreads() {
        Iterator<Map.Entry<String, CacheEntry>> it = threadCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CacheEntry> entry = it.next();
            if (entry.getValue().isExpired()) {
                deleteThreadFromOpenAI(entry.getValue().threadId);
                it.remove();
            }
        }
    }

    // Método auxiliar para exclusão
    private void deleteThreadFromOpenAI(String threadId) {
        try {
            restTemplate.exchange(
                    BASE_URL + "/threads/" + threadId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(getHeaders()),
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Erro ao excluir thread: " + e.getMessage());
        }
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

    public String uploadFileToVectorStore(String fileName, byte[] fileContent, String vectorStoreId) {
        try {
            // 1. Upload do arquivo
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            fileHeaders.setBearerAuth(API_KEY);

            MultiValueMap<String, Object> fileBody = new LinkedMultiValueMap<>();
            fileBody.add("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            fileBody.add("purpose", "assistants");

            HttpEntity<MultiValueMap<String, Object>> fileEntity = new HttpEntity<>(fileBody, fileHeaders);

            // Executa o upload inicial
            ResponseEntity<Map> fileResponse = restTemplate.exchange(
                    BASE_URL + "/files",
                    HttpMethod.POST,
                    fileEntity,
                    Map.class
            );

            if (fileResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Falha no upload do arquivo: " + fileResponse.getStatusCode());
            }

            String fileId = (String) fileResponse.getBody().get("id");
            System.out.println("Arquivo enviado. ID: " + fileId);

            // 2. Associar ao Vector Store
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
            jsonHeaders.setBearerAuth(API_KEY);

            Map<String, String> associationBody = Collections.singletonMap("file_id", fileId);
            HttpEntity<Map<String, String>> associationEntity = new HttpEntity<>(associationBody, jsonHeaders);

            ResponseEntity<Map> associationResponse = restTemplate.exchange(
                    BASE_URL + "/vector_stores/" + vectorStoreId + "/files",
                    HttpMethod.POST,
                    associationEntity,
                    Map.class
            );
            System.out.println("Associação ao vector store " + vectorStoreId + " bem-sucedida.");

            // 3. Verificar status até estar "completed"
            int attempts = 0;
            while (attempts < 30) { // Timeout de 30 segundos
                try {
                    ResponseEntity<Map> statusResponse = restTemplate.exchange(
                            BASE_URL + "/vector_stores/" + vectorStoreId + "/files/" + fileId,
                            HttpMethod.GET,
                            new HttpEntity<>(getHeaders()),
                            Map.class
                    );

                    String status = (String) statusResponse.getBody().get("status");
                    System.out.println("Status do arquivo " + fileId + ": " + status);

                    if ("completed".equals(status)) {
                        System.out.println("Arquivo pronto para uso!");
                        return fileId;
                    }

                    Thread.sleep(1000); // Aguarda 1 segundo entre as verificações
                    attempts++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Processo interrompido durante a verificação de status", e);
                }
            }

            throw new RuntimeException("Timeout: Arquivo não processado após 30 segundos.");

        } catch (Exception e) {
            System.err.println("Erro crítico durante o upload:");
            e.printStackTrace();
            throw new RuntimeException("Falha completa no upload: " + e.getMessage(), e);
        }
    }
    // OpenAIAssistantService.java
    public String uploadJsonToVectorStore(String fileName, String jsonContent, String vectorStoreId) {
        return uploadFileToVectorStore(fileName, jsonContent.getBytes(StandardCharsets.UTF_8), vectorStoreId);
    }
}