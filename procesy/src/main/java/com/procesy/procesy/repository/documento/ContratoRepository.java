package com.procesy.procesy.repository.documento;

import com.procesy.procesy.dto.ContratoDTO;
import com.procesy.procesy.model.documentos.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    //findContratosByDocumentoProcessoId
    @Query("select new com.procesy.procesy.dto.ContratoDTO(c.id, c.nomeArquivo, c.tipoArquivo) " +
            "from Contrato c where c.documentoProcesso.id = :documentoProcessoId")
    List<ContratoDTO> findContratosByDocumentoProcessoId(@Param("documentoProcessoId") Long documentoProcessoId);
}
