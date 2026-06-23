package com.projeto.abencoar.domain.repository;

import com.projeto.abencoar.domain.model.Agendamento;
import com.projeto.abencoar.domain.model.StatusInadimplencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Busca todos os horários de um dia específico (útil para o dashboard)
    List<Agendamento> findByDataOrderByHorarioAsc(LocalDate data);

    // O Spring transforma esse nome em: SELECT * FROM zab_agendamentos ORDER BY data ASC, horario ASC
    List<Agendamento> findAllByOrderByDataAscHorarioAsc();

    // Busca horários vago de uma especialidade
    List<Agendamento> findByEspecialidadeIdAndBeneficiarioIsNull(Long especialidadeId);

    // IMPORTANTE: Método que ordena por Especialidade
    List<Agendamento> findAllByDataOrderByEspecialidadeNomeAscHorarioAsc(LocalDate data);

    List<Agendamento> findAllByDataAndEspecialidadeIdOrderByHorarioAsc(LocalDate data, Long especialidadeId);

    // 🛡️ FILTRO AVANÇADO UNIFICADO (Dentro das chaves da Interface e usando LEFT JOIN)
    @Query("SELECT a FROM Agendamento a LEFT JOIN a.beneficiario b WHERE " +
            "(:especialidadeId IS NULL OR a.especialidade.id = :especialidadeId) AND " +
            "(:dataBusca IS NULL OR a.data = :dataBusca) AND " +
            "(:nome IS NULL OR LOWER(CAST(b.nome AS string)) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%'))) AND " +
            "(:rua IS NULL OR LOWER(CAST(b.endereco AS string)) LIKE LOWER(CONCAT('%', CAST(:rua AS string), '%'))) AND " +
            "(:inadimplencia IS NULL OR b.statusInadimplencia = :inadimplencia) AND " +
            "(:mesAniversario IS NULL OR MONTH(b.dataNascimento) = :mesAniversario) AND " +
            "(:diaAniversario IS NULL OR DAY(b.dataNascimento) = :diaAniversario) " +
            "ORDER BY a.data ASC, a.horario ASC")
    List<Agendamento> buscarComFiltrosAvancados(
            @Param("especialidadeId") Long especialidadeId,
            @Param("dataBusca") LocalDate dataBusca,
            @Param("nome") String nome,
            @Param("rua") String rua,
            @Param("inadimplencia") StatusInadimplencia inadimplencia,
            @Param("mesAniversario") Integer mesAniversario,
            @Param("diaAniversario") Integer diaAniversario
    );
} // <--- Apenas esta chave fecha o arquivo agora!