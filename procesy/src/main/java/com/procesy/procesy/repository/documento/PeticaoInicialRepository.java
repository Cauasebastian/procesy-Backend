package com.procesy.procesy.repository.documento;

import com.procesy.procesy.model.documentos.PeticaoInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeticaoInicialRepository extends JpaRepository<PeticaoInicial, Long> {
}
