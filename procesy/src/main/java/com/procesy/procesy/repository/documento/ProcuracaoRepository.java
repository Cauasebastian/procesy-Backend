package com.procesy.procesy.repository.documento;

import com.procesy.procesy.dto.ProcuracaoDTO;
import com.procesy.procesy.model.documentos.Procuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface ProcuracaoRepository extends JpaRepository<Procuracao, Long> {
    @Query("select new com.procesy.procesy.dto.ProcuracaoDTO(p.id, p.nomeArquivo, p.tipoArquivo) " +
            "from Procuracao p where p.documentoProcesso.id = :documentoProcessoId")
    List<ProcuracaoDTO> findProcuracoesByDocumentoProcessoId(@Param("documentoProcessoId") Long documentoProcessoId);
}
