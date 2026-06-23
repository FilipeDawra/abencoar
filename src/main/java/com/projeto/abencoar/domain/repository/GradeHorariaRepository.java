package com.projeto.abencoar.domain.repository;

import com.projeto.abencoar.domain.model.GradeHoraria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;

public interface GradeHorariaRepository extends JpaRepository<GradeHoraria, Long> {
    List<GradeHoraria> findByEspecialidadeId(Long especialidadeId);

    // 🎯 ADICIONE ESTA LINHA: Essencial para o motor mapear os dias do mês
    List<GradeHoraria> findByDiaSemanaAndAtivoTrue(DayOfWeek diaSemana);
}