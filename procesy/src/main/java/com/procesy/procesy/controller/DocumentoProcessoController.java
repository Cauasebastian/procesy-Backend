package com.procesy.procesy.controller;

import com.procesy.procesy.service.DocumentoProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * @param processoId                 ID do Processo.
     * @param procuracoesFiles           Lista de arquivos de procuração.
     * @param peticoesIniciaisFiles      Lista de arquivos de petição inicial.
     * @param documentosComplementaresFiles Lista de arquivos de documentos complementares.
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
        }
    }
}
