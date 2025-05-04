package com.procesy.procesy.service.cliente;

import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.repository.ClienteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClienteDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public ClienteDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
        return User.builder()
                .username(cliente.getEmail())
                .password(cliente.getSenha())
                .roles("CLIENT")
                .build();
    }
}