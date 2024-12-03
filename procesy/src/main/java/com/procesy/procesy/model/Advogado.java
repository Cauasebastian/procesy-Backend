package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "advogado")
@Data
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
    @JsonBackReference
    private List<Cliente> clientes;
}
