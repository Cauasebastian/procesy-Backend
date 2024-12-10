package com.procesy.procesy.repository.documento;

import com.procesy.procesy.model.documentos.Procuracao;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface ProcuracaoRepository extends JpaRepository<Procuracao, Long> {
}
