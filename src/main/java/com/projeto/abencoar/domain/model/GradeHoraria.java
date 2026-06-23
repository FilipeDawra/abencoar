package com.projeto.abencoar.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "zab_grades_horarias")
@Data
public class GradeHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;

    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana; // Preenchido se for recorrente (ex: TUESDAY)

    private LocalDate dataEspecifica; // 🎯 NOVO: Preenchido se for um atendimento clínico isolado (ex: 19/06/2026)

    @Enumerated(EnumType.STRING)
    private Periodo periodo;

    private LocalTime horarioInicio;
    private Integer vagasTotais;
    private String profissionalPadrao; // Certo!
    private boolean ativo = true;
}