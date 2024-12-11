package com.procesy.procesy.repository.documento;


import com.procesy.procesy.model.documentos.DocumentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentoProcessoRepository extends JpaRepository<DocumentoProcesso, Long> {
    @Query("SELECT dp FROM DocumentoProcesso dp " +
            "LEFT JOIN FETCH dp.procuracoes " +
            "LEFT JOIN FETCH dp.peticoesIniciais " +
            "LEFT JOIN FETCH dp.documentosComplementares " +
            "WHERE dp.processo.id = :processoId")
    Optional<DocumentoProcesso> findByProcessoIdWithDocuments(@Param("processoId") Long processoId);
}