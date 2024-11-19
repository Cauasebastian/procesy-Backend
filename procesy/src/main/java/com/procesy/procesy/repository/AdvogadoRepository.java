package com.procesy.procesy.repository;

import com.procesy.procesy.model.Advogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvogadoRepository extends JpaRepository<Advogado, Long> {
    Optional<Advogado> findByEmail(String email);
}
