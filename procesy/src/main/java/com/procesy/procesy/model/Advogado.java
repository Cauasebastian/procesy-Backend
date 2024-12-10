package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "advogado")
public class Advogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    @NotEmpty(message = "O nome é obrigatório.")
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    @NotEmpty(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    private String email;

    @Column(name = "senha", nullable = false)
    @NotEmpty(message = "A senha é obrigatória.")
    private String senha;

    @OneToMany(mappedBy = "advogado", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("advogado-clientes")
    private List<Cliente> clientes = new ArrayList<>();
}
