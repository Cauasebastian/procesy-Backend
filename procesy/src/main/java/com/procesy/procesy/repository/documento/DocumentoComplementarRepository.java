package com.procesy.procesy.repository.documento;

import com.procesy.procesy.model.DocumentoComplementar;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface DocumentoComplementarRepository extends JpaRepository<DocumentoComplementar, Long> {
}
