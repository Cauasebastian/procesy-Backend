package com.procesy.procesy.service;

import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.repository.AdvogadoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdvogadoDetailsService implements UserDetailsService {

    private final AdvogadoRepository advogadoRepository;

    public AdvogadoDetailsService(AdvogadoRepository advogadoRepository) {
        this.advogadoRepository = advogadoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Advogado advogado = advogadoRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return org.springframework.security.core.userdetails.User
                .withUsername(advogado.getEmail())
                .password(advogado.getSenha())
                .roles("USER") // Pode adicionar mais roles se necessário
                .build();
    }
}
