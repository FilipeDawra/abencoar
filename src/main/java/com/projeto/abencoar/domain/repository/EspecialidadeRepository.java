package com.projeto.abencoar.domain.repository;

import com.projeto.abencoar.domain.model.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
    Optional<Especialidade> findByNome(String nome);
}