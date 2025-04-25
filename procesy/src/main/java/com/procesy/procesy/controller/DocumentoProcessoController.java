package com.procesy.procesy.controller;

import com.procesy.procesy.dto.DocumentoProcessoDTO;
import com.procesy.procesy.model.documentos.Contrato;
import com.procesy.procesy.model.documentos.DocumentoComplementar;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import com.procesy.procesy.model.documentos.Procuracao;
import com.procesy.procesy.service.DocumentoProcessoService;
import com.procesy.procesy.service.OpenAIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/documento-processo")
public class DocumentoProcessoController {

    @Autowired
    private DocumentoProcessoService documentoProcessoService;

    /**
     * Endpoint para upload de documentos e vinculação ao Processo.
     *
     * @param processoId                      ID do Processo.
     * @param procuracoesFiles                Lista de arquivos de procuração.
     * @param peticoesIniciaisFiles           Lista de arquivos de petição inicial.
     * @param documentosComplementaresFiles    Lista de arquivos de documentos complementares.
     * @param contratosFiles                  Lista de arquivos de contrato.
     * @return ResponseEntity com status apropriado.
     */
    @PostMapping("/upload/{processoId}")
    public ResponseEntity<String> uploadDocumentosProcesso(
            @PathVariable Long processoId,
            @RequestParam(value = "procuracoes", required = false) List<MultipartFile> procuracoesFiles,
            @RequestParam(value = "peticoesIniciais", required = false) List<MultipartFile> peticoesIniciaisFiles,
            @RequestParam(value = "documentosComplementares", required = false) List<MultipartFile> documentosComplementaresFiles,
            @RequestParam(value = "contratos", required = false) List<MultipartFile> contratosFiles
    ) {
        // Garante que as listas nunca sejam nulas
        procuracoesFiles = procuracoesFiles != null ? procuracoesFiles : Collections.emptyList();
        peticoesIniciaisFiles = peticoesIniciaisFiles != null ? peticoesIniciaisFiles : Collections.emptyList();
        documentosComplementaresFiles = documentosComplementaresFiles != null ? documentosComplementaresFiles : Collections.emptyList();
        contratosFiles = contratosFiles != null ? contratosFiles : Collections.emptyList();

        try {
            documentoProcessoService.adicionarDocumentosAoProcesso(
                    processoId,
                    procuracoesFiles,
                    peticoesIniciaisFiles,
                    documentosComplementaresFiles,
                    contratosFiles
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Documentos enviados com sucesso.");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar os documentos de um Processo específico.
     *
     * @param processoId ID do Processo.
     * @return DocumentoProcessoDTO contendo listas de IDs e metadados dos documentos.
     */
    @GetMapping("/processo/{processoId}")
    public ResponseEntity<?> listarDocumentosDoProcesso(@PathVariable Long processoId) {
        try {
            DocumentoProcessoDTO documentoProcessoDTO = documentoProcessoService.getDocumentosDoProcessoDTO(processoId);
            return ResponseEntity.ok(documentoProcessoDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Endpoint para baixar uma Procuracao específica.
     *
     * @param procuracaoId ID da Procuracao.
     * @return Arquivo da Procuracao.
     */
    @GetMapping("/procuracao/{procuracaoId}")
    public ResponseEntity<byte[]> baixarProcuracao(@PathVariable Long procuracaoId) {
        try {
            Procuracao procuracao = documentoProcessoService.getProcuracaoById(procuracaoId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(procuracao.getTipoArquivo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + procuracao.getNomeArquivo() + "\"")
                    .body(procuracao.getArquivo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Endpoint para baixar uma PeticaoInicial específica.
     *
     * @param peticaoId ID da PeticaoInicial.
     * @return Arquivo da PeticaoInicial.
     */
    @GetMapping("/peticao-inicial/{peticaoId}")
    public ResponseEntity<byte[]> baixarPeticaoInicial(@PathVariable Long peticaoId) {
        try {
            PeticaoInicial peticaoInicial = documentoProcessoService.getPeticaoInicialById(peticaoId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(peticaoInicial.getTipoArquivo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + peticaoInicial.getNomeArquivo() + "\"")
                    .body(peticaoInicial.getArquivo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Endpoint para baixar um DocumentoComplementar específico.
     *
     * @param documentoComplementarId ID do DocumentoComplementar.
     * @return Arquivo do DocumentoComplementar.
     */
    @GetMapping("/documento-complementar/{documentoComplementarId}")
    public ResponseEntity<byte[]> baixarDocumentoComplementar(@PathVariable Long documentoComplementarId) {
        try {
            DocumentoComplementar documentoComplementar = documentoProcessoService.getDocumentoComplementarById(documentoComplementarId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(documentoComplementar.getTipoArquivo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentoComplementar.getNomeArquivo() + "\"")
                    .body(documentoComplementar.getArquivo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Endpoint para baixar um Contrato específico.
     *
     * @param contratoId ID do Contrato.
     * @return Arquivo do Contrato.
     */
    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<byte[]> baixarContrato(@PathVariable Long contratoId) {
        try {
            Contrato contrato = documentoProcessoService.getContratoById(contratoId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contrato.getTipoArquivo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + contrato.getNomeArquivo() + "\"")
                    .body(contrato.getArquivo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
