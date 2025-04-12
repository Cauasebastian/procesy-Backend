package com.procesy.procesy.service;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.repository.AdvogadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdvogadoService {

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return advogadoRepository.findByEmail(email).isPresent();
    }

    public Advogado salvarAdvogado(Advogado advogado) {
        // Criptografa a senha antes de salvar
        advogado.setSenha(passwordEncoder.encode(advogado.getSenha()));
        return advogadoRepository.save(advogado);
    }

    public Advogado atualizarAdvogado(Advogado advogado) {
        // Atualiza os campos sem recriptografar a senha
        return advogadoRepository.save(advogado);
    }
    public Optional<Advogado> findByEmail(String email) {
        return advogadoRepository.findByEmail(email);
    }

    public Optional<Advogado> findById(Long id) {
        return advogadoRepository.findById(id);
    }

    // Outros métodos conforme necessário (e.g., atualizar, deletar Advogado)
}
