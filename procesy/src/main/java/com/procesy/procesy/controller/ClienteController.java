package com.procesy.procesy.controller;


import com.procesy.procesy.dto.ProcessoDTO;
import com.procesy.procesy.service.ProcessoService;
import com.procesy.procesy.service.cliente.ClienteProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final ClienteProcessoService clienteProcessoService;


    public ClienteController(ClienteProcessoService clienteProcessoService) {
        this.clienteProcessoService = clienteProcessoService;
    }


    //getProcessos
    @RequestMapping("/processos")
    public ResponseEntity<List<ProcessoDTO>> getProcessos(Authentication authentication) {
        String email = authentication.getName();
        List<ProcessoDTO> processos = clienteProcessoService.getClienteProcessos(email);
        return ResponseEntity.ok(processos);
    }
    //getProcessoById
    @RequestMapping("/processos/{id}")
    public ResponseEntity<ProcessoDTO> getProcessoById(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        ProcessoDTO processo = clienteProcessoService.getClienteProcessoById(id, email);
        return ResponseEntity.ok(processo);
    }
}
