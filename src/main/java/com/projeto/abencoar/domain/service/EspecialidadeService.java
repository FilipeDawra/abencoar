package com.projeto.abencoar.domain.service;

import com.projeto.abencoar.domain.model.Especialidade;
import com.projeto.abencoar.domain.repository.EspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    @Transactional
    public Especialidade salvar(Especialidade especialidade) {
        // Regra: Nome da especialidade deve ser único
        boolean nomeEmUso = especialidadeRepository.findByNome(especialidade.getNome())
                .stream()
                .anyMatch(e -> !e.equals(especialidade));

        if (nomeEmUso) {
            throw new RuntimeException("Já existe uma especialidade com o nome: " + especialidade.getNome());
        }

        return especialidadeRepository.save(especialidade);
    }

    public Especialidade buscarPorNome(String nome) {
        return especialidadeRepository.findByNome(nome)
                .orElseThrow(() -> new RuntimeException("Especialidade não encontrada: " + nome));
    }

    public List<Especialidade> listarTodas() {
        return especialidadeRepository.findAll();
    }
}