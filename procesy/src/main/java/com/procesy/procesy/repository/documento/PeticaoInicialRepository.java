package com.procesy.procesy.repository.documento;

import com.procesy.procesy.dto.PeticaoInicialDTO;
import com.procesy.procesy.model.documentos.PeticaoInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeticaoInicialRepository extends JpaRepository<PeticaoInicial, Long> {
    //findPeticoesIniciaisByDocumentoProcessoId
    @Query("select new com.procesy.procesy.dto.PeticaoInicialDTO(pi.id, pi.nomeArquivo, pi.tipoArquivo) " +
            "from PeticaoInicial pi where pi.documentoProcesso.id = :documentoProcessoId")
    List<PeticaoInicialDTO> findPeticoesIniciaisByDocumentoProcessoId(@Param("documentoProcessoId") Long documentoProcessoId);
}
