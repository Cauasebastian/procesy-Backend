package com.procesy.procesy.repository.documento;

import com.procesy.procesy.dto.DocumentoComplementarDTO;
import com.procesy.procesy.model.documentos.DocumentoComplementar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface DocumentoComplementarRepository extends JpaRepository<DocumentoComplementar, Long> {
    //findDocumentosComplementaresByDocumentoProcessoId
    @Query("select new com.procesy.procesy.dto.DocumentoComplementarDTO(dc.id, dc.nomeArquivo, dc.tipoArquivo) " +
            "from DocumentoComplementar dc where dc.documentoProcesso.id = :documentoProcessoId")
    List<DocumentoComplementarDTO> findDocumentosComplementaresByDocumentoProcessoId(@Param("documentoProcessoId") Long documentoProcessoId);
}
