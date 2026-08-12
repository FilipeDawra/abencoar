package com.projeto.abencoar.domain.service;

import com.projeto.abencoar.domain.model.Beneficiario;
import com.projeto.abencoar.domain.model.Cpf;
import com.projeto.abencoar.domain.model.Telefone;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    @Transactional
    public Beneficiario salvar(Beneficiario beneficiario) {

        // 🧹 HIGIENIZAÇÃO (Sanitization): Remove pontos, traços e parênteses
        if (beneficiario.getCpf() != null && beneficiario.getCpf().getNumero() != null) {
            String cpfLimpo = beneficiario.getCpf().getNumero().replaceAll("\\D", "");
            beneficiario.setCpf(new Cpf(cpfLimpo));
        }

        if (beneficiario.getTelefone() != null && beneficiario.getTelefone().getNumero() != null) {
            String telefoneLimpo = beneficiario.getTelefone().getNumero().replaceAll("\\D", "");
            beneficiario.setTelefone(new Telefone(telefoneLimpo));
        }

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