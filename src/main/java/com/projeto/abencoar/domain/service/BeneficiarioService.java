package com.projeto.abencoar.domain.service;

import com.projeto.abencoar.domain.model.Beneficiario;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Cria o construtor para o Repository automaticamente
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    @Transactional // Garante que, se der erro no meio, o banco não fica "sujo"
    public Beneficiario salvar(Beneficiario beneficiario) {

        // REGRA DE OURO: Não permitir CPF duplicado
        boolean cpfEmUso = repository.findByCpfNumero(beneficiario.getCpf().getNumero())
                .stream()
                .anyMatch(b -> !b.equals(beneficiario));

        if (cpfEmUso) {
            throw new RuntimeException("Já existe um beneficiário cadastrado com este CPF!");
        }

        return repository.save(beneficiario);
    }
}