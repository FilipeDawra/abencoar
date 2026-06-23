package com.projeto.abencoar.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "zab_especialidades")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome; // Ex: "Pilates", "Psicologia"

    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;
}