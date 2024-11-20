package com.procesy.procesy.repository;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByAdvogado(Advogado advogado);
}
