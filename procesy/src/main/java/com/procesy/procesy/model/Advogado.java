package com.procesy.procesy.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "advogado")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Advogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    // Armazena a chave pública do advogado para criptografia
    @Lob
    @Column(name = "public_key", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] publicKey;


    @OneToMany(mappedBy = "advogado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cliente> clientes = new ArrayList<>();
}
