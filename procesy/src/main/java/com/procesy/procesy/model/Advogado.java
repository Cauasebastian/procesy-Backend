package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @OneToMany(mappedBy = "advogado", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Cliente> clientes;
}
