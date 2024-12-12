package com.procesy.procesy.repository.documento;

import com.procesy.procesy.model.documentos.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
}
