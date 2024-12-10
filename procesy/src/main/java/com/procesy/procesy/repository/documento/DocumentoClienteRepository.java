package com.procesy.procesy.repository.documento;

import com.procesy.procesy.model.DocumentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;


@org.springframework.stereotype.Repository
public interface DocumentoClienteRepository extends JpaRepository<DocumentoCliente, Long> {
}
