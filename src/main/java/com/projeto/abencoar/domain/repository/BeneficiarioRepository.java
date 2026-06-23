package com.projeto.abencoar.domain.repository;

import com.projeto.abencoar.domain.model.Beneficiario;
import com.projeto.abencoar.domain.model.StatusInadimplencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// 🚀 Trocado de Long para String para casar com a chave primária 'matricula'
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, String> {

    // Busca pelo CPF baseado no Value Object
    Optional<Beneficiario> findByCpfNumero(String numero);

    // Filtros Simples
    List<Beneficiario> findByNomeContainingIgnoreCase(String nome);

    List<Beneficiario> findByEnderecoContainingIgnoreCase(String endereco);

    List<Beneficiario> findByStatusInadimplencia(StatusInadimplencia status);

    // 🛡️ FILTRO AVANÇADO BLINDADO CONTRA BYTEA (Garante o ILIKE para todas as Marias)
    @Query("SELECT b FROM Beneficiario b WHERE " +
            "(CAST(:nome AS string) IS NULL OR b.nome ILIKE CONCAT('%', CAST(:nome AS string), '%')) AND " +
            "(CAST(:rua AS string) IS NULL OR b.endereco ILIKE CONCAT('%', CAST(:rua AS string), '%')) AND " +
            "(:inadimplencia IS NULL OR b.statusInadimplencia = :inadimplencia) AND " +
            "(:mesAniversario IS NULL OR MONTH(b.dataNascimento) = :mesAniversario) AND " +
            "(:diaAniversario IS NULL OR DAY(b.dataNascimento) = :diaAniversario) AND " +
            "(CAST(:especialidade AS string) IS NULL OR b.nomeResponsavel ILIKE CAST(:especialidade AS string)) " +
            "ORDER BY b.nome ASC")
    List<Beneficiario> buscarBeneficiariosComFiltrosAvancados(
            @Param("nome") String nome,
            @Param("rua") String rua,
            @Param("inadimplencia") StatusInadimplencia inadimplencia,
            @Param("mesAniversario") Integer mesAniversario,
            @Param("diaAniversario") Integer diaAniversario,
            @Param("especialidade") String especialidade
    );
}