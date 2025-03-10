package com.procesy.procesy.repository.documento;


import com.procesy.procesy.dto.DocumentoProcessoDTO;
import com.procesy.procesy.model.documentos.DocumentoProcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentoProcessoRepository extends JpaRepository<DocumentoProcesso, Long> {
    @Query("select new com.procesy.procesy.dto.DocumentoProcessoDTO(" +
            "dp.id, dp.processo.id, dp.statusContrato, dp.statusProcuracoes, " +
            "dp.statusPeticoesIniciais, dp.statusDocumentosComplementares) " +
            "from DocumentoProcesso dp where dp.processo.id = :processoId")
    Optional<DocumentoProcessoDTO> findDocumentoProcessoDTOByProcessoId(@Param("processoId") Long processoId);

}