package com.procesy.procesy.service.advogado;

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
                .orElseThrow(() -> {
                    System.out.println("Advogado não encontrado para email: " + email);
                    return new UsernameNotFoundException("Usuário não encontrado");
                });
        System.out.println("Advogado encontrado: " + advogado.getEmail() + " - senha: " + advogado.getSenha());
        return org.springframework.security.core.userdetails.User
                .withUsername(advogado.getEmail())
                .password(advogado.getSenha())
                .roles("ADVOGADO")
                .build();
    }

}
