package com.procesy.procesy.controller;

import com.procesy.procesy.dto.MLResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.procesy.procesy.dto.MLRequestDTO;
import com.procesy.procesy.model.Processo;
import com.procesy.procesy.repository.ProcessoRepository;
import com.procesy.procesy.service.ProcessoService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/machine-learning")
public class MachineLearningController {

    @Value("${ml.api.url}") // Configure no application.properties
    private String mlApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ProcessoService processoService;

    /**
     * Previsão de tempo de resposta para um processo
     *
     * @param processoId ID do processo
     * @return Tempo previsto em dias
     */
    @PostMapping("/previsao/{processoId}")
    public ResponseEntity<?> preverTempoResposta(@PathVariable Long processoId) {
        Optional<Processo> processoOpt = processoRepository.findById(processoId);
        if (processoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Processo processo = processoOpt.get();
        MLRequestDTO request = createMLRequest(processo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MLRequestDTO[]> httpEntity = new HttpEntity<>(
                new MLRequestDTO[]{request},
                headers
        );

        try {
            // Alterado para MLResponseDTO[]
            ResponseEntity<MLResponseDTO[]> response = restTemplate.exchange(
                    mlApiUrl + "/prever-tempo-resposta",
                    HttpMethod.POST,
                    httpEntity,
                    MLResponseDTO[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MLResponseDTO[] resultados = response.getBody();

                if (resultados.length > 0) {
                    // Converte para inteiro arredondando o valor
                    int previsaoDias = (int) Math.round(resultados[0].getTempoRespostaDias());
                    System.out.println("Previsão recebida: " + previsaoDias + " dias");
                    return ResponseEntity.ok(previsaoDias);
                } else {
                    System.out.println("Resposta vazia do modelo de ML");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Resposta vazia do modelo");
                }
            } else {
                System.out.println("Erro na resposta ML: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }
        } catch (Exception e) {
            System.out.println("Erro na chamada ML: " + e.getMessage());
            e.printStackTrace(); // Log completo do erro
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Serviço de ML indisponível");
        }
    }

    /**
     * Cria objeto de requisição para o modelo de ML
     */
    private MLRequestDTO createMLRequest(Processo processo) {
        MLRequestDTO request = new MLRequestDTO();

        // Converter java.sql.Date para LocalDate de forma segura
        Instant dataInicioInstant = convertToStartOfDayInstant(processo.getDataInicio());
        long diasInicio = ChronoUnit.DAYS.between(dataInicioInstant, Instant.now());

        // Usar dataAtualizacao ou dataInicio se não houver atualização
        Date dataParaAtualizacao = processo.getDataAtualizacao() != null ?
                processo.getDataAtualizacao() :
                processo.getDataInicio();

        Instant dataAtualizacaoInstant = convertToStartOfDayInstant(dataParaAtualizacao);
        long diasAtualizacao = ChronoUnit.DAYS.between(dataAtualizacaoInstant, Instant.now());

        // Preencher DTO
        request.setTipoProcesso(processo.getTipoProcesso());
        request.setTipoAtendimento(processo.getTipoAtendimento());
        request.setStatus(processo.getStatus());
        request.setTempoInicioDias(diasInicio);
        request.setTempoAtualizacaoDias(diasAtualizacao);

        if (processo.getDocumentoProcesso() != null) {
            request.setStatusContrato(processo.getDocumentoProcesso().getStatusContrato());
            request.setStatusProcuracoes(processo.getDocumentoProcesso().getStatusProcuracoes());
            request.setStatusPeticoesIniciais(processo.getDocumentoProcesso().getStatusPeticoesIniciais());
            request.setStatusDocumentosComplementares(processo.getDocumentoProcesso().getStatusDocumentosComplementares());
        }
        // Debug: Exibir o corpo da requisição antes de enviá-la
        System.out.println("Corpo da requisição: " + request);

        return request;
    }

    // Método auxiliar para converter Date para Instant no início do dia
    private Instant convertToStartOfDayInstant(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

}