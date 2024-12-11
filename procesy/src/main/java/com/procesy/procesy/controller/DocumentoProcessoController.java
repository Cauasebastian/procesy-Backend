package com.procesy.procesy.controller;

import com.procesy.procesy.model.documentos.DocumentoComplementar;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import com.procesy.procesy.model.documentos.Procuracao;
import com.procesy.procesy.service.DocumentoProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * @return ResponseEntity com status apropriado.
     */
    @PostMapping("/upload/{processoId}")
    public ResponseEntity<String> uploadDocumentosProcesso(
            @PathVariable Long processoId,
            @RequestParam("procuracoes") List<MultipartFile> procuracoesFiles,
            @RequestParam("peticoesIniciais") List<MultipartFile> peticoesIniciaisFiles,
            @RequestParam("documentosComplementares") List<MultipartFile> documentosComplementaresFiles
    ) {
        System.out.println("Recebendo arquivos...");
        try {
            documentoProcessoService.adicionarDocumentosAoProcesso(processoId,
                    procuracoesFiles, peticoesIniciaisFiles, documentosComplementaresFiles);
            System.out.println("Documentos enviados com sucesso.");
            return ResponseEntity.status(HttpStatus.CREATED).body("Documentos enviados com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar os arquivos.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar os documentos de um Processo específico.
     *
     * @param processoId ID do Processo.
     * @return DocumentoProcesso contendo listas de documentos.
     */
    @GetMapping("/processo/{processoId}")
    public ResponseEntity<?> listarDocumentosDoProcesso(@PathVariable Long processoId) {
        try {
            DocumentoProcesso documentoProcesso = documentoProcessoService.getDocumentosDoProcesso(processoId);
            return ResponseEntity.ok(documentoProcesso);
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
        }
    }
}
