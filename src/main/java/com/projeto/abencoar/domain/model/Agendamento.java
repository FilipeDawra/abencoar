package com.projeto.abencoar.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "zab_agendamentos")
@Data
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;

    @ManyToOne // 🛡️ REMOVIDO O CASCADE! Isso impede o Hibernate de tentar reinserir o beneficiário por engano
    @JoinColumn(name = "beneficiario_id", referencedColumnName = "matricula")
    private Beneficiario beneficiario;

    private LocalDate data;
    private LocalTime horario;
    private String profissional;

    @Enumerated(EnumType.STRING)
    private Periodo periodo;

    private boolean realizado = false;
}