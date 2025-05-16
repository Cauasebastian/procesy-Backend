package com.procesy.procesy.service.cliente;

import com.procesy.procesy.dto.ClienteDTO;
import com.procesy.procesy.model.Advogado;
import com.procesy.procesy.model.Cliente;
import com.procesy.procesy.repository.ClienteRepository;
import com.procesy.procesy.service.advogado.AdvogadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AdvogadoService advogadoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //findById
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    /**
     * Retorna todos os Clientes associados a um Advogado específico.
     *
     * @param advogadoId ID do Advogado
     * @return Lista de Clientes
     */
    public List<Cliente> getClientesByAdvogadoId(Long advogadoId) {
        Advogado advogado = advogadoService.findById(advogadoId)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        return clienteRepository.findByAdvogado(advogado);
    }

    /**
     * Cria um novo Cliente associado a um Advogado.
     *
     * @param cliente   Dados do Cliente
     * @param advogadoId ID do Advogado
     * @return Cliente salvo
     */
    @Transactional
    public Cliente criarCliente(Cliente cliente, Long advogadoId) {
        Advogado advogado = advogadoService.findById(advogadoId)
                .orElseThrow(() -> new RuntimeException("Advogado não encontrado"));
        cliente.setAdvogado(advogado);
        return clienteRepository.save(cliente);
    }
    @Transactional
    public Cliente register(String email, String senha) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        // Criptografa a senha antes de salvar
        cliente.setSenha(passwordEncoder.encode(senha));

        return clienteRepository.save(cliente);
    }
    /**
     * Atualiza um Cliente existente, garantindo que o Advogado seja o proprietário.
     *
     * @param clienteId        ID do Cliente a ser atualizado
     * @param clienteAtualizado Dados atualizados do Cliente
     * @param advogadoId        ID do Advogado autenticado
     * @return Cliente atualizado
     */
    @Transactional
    public Cliente atualizarCliente(Long clienteId, Cliente clienteAtualizado, Long advogadoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAdvogado().getId().equals(advogadoId)) {
            throw new RuntimeException("Acesso negado: Advogado não é proprietário do Cliente");
        }

        // Atualiza os campos necessários
        cliente.setNome(clienteAtualizado.getNome());
        cliente.setGenero(clienteAtualizado.getGenero());
        cliente.setEstadoCivil(clienteAtualizado.getEstadoCivil());
        cliente.setCpf_cnpj(clienteAtualizado.getCpf_cnpj());
        cliente.setNaturalidade(clienteAtualizado.getNaturalidade());
        cliente.setDataNascimento(clienteAtualizado.getDataNascimento());
        cliente.setTelefone(clienteAtualizado.getTelefone());
        cliente.setEmail(clienteAtualizado.getEmail());

        return clienteRepository.save(cliente);
    }

    /**
     * Deleta um Cliente existente, garantindo que o Advogado seja o proprietário.
     *
     * @param clienteId  ID do Cliente a ser deletado
     * @param advogadoId ID do Advogado autenticado
     */
    @Transactional
    public void deletarCliente(Long clienteId, Long advogadoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAdvogado().getId().equals(advogadoId)) {
            throw new RuntimeException("Acesso negado: Advogado não é proprietário do Cliente");
        }

        clienteRepository.delete(cliente);
    }

    /**
     * Retorna um Cliente específico, garantindo que o Advogado seja o proprietário.
     *
     * @param clienteId  ID do Cliente
     * @param advogadoId ID do Advogado autenticado
     * @return Cliente encontrado
     */
    public Cliente getClienteById(Long clienteId, Long advogadoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getAdvogado().getId().equals(advogadoId)) {
            throw new RuntimeException("Acesso negado: Advogado não é proprietário do Cliente");
        }

        return cliente;
    }

    public ClienteDTO convertToDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setTelefone(cliente.getTelefone());
        dto.setEmail(cliente.getEmail());
        dto.setQuantidadeProcessos(cliente.getProcessos() != null ? cliente.getProcessos().size() : 0);
        return dto;
    }
}
