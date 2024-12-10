package com.procesy.procesy.repository.documento;


import com.procesy.procesy.model.documentos.DocumentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoProcessoRepository extends JpaRepository<DocumentoProcesso, Long> {
}
