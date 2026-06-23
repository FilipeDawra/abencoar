package com.projeto.abencoar.domain.service;

import com.projeto.abencoar.domain.model.*;
import com.projeto.abencoar.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final GradeHorariaRepository gradeRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public void gerarAgendaMensal(int mes, int ano) {
        List<GradeHoraria> grades = gradeRepository.findAll().stream()
                .filter(GradeHoraria::isAtivo)
                .toList();

        LocalDate dataInicio = LocalDate.of(ano, mes, 1);
        LocalDate dataFim = dataInicio.withDayOfMonth(dataInicio.lengthOfMonth());

        // Loop pelos dias do mês para as grades fixas (Pilates, Ballet...)
        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            for (GradeHoraria grade : grades) {
                // Se for grade recorrente por Dia da Semana
                if (grade.getDiaSemana() != null && data.getDayOfWeek() == grade.getDiaSemana()) {
                    criarEspacosDeAgendamento(data, grade);
                }
            }
        }

        // Loop único para as grades de datas específicas (Psicologia Clínica, eventos...)
        for (GradeHoraria grade : grades) {
            if (grade.getDataEspecifica() != null) {
                // Só gera se a data específica estiver dentro do mês que o Bil está gerando
                if (grade.getDataEspecifica().getMonthValue() == mes && grade.getDataEspecifica().getYear() == ano) {
                    criarEspacosDeAgendamento(grade.getDataEspecifica(), grade);
                }
            }
        }
    }

    // 🎯 Método auxiliar único para criar os slots de vagas no banco
    private void criarEspacosDeAgendamento(LocalDate data, GradeHoraria grade) {
        for (int i = 0; i < grade.getVagasTotais(); i++) {
            Agendamento agendamento = new Agendamento();
            agendamento.setEspecialidade(grade.getEspecialidade());
            agendamento.setData(data);
            agendamento.setHorario(grade.getHorarioInicio());
            agendamento.setProfissional(grade.getProfissionalPadrao());
            agendamento.setPeriodo(grade.getPeriodo());
            agendamento.setRealizado(false);

            agendamentoRepository.save(agendamento);
        }
    }

    // 🛡️ O "Casamento" do beneficiário com o horário gerado (Atualizado para Matrícula String)
    @Transactional
    public Agendamento realizarAgendamento(Long agendamentoId, String beneficiarioId) { // 🚀 Mudado para String aqui
        // 1. Verificar se o horário existe
        // Se o seu agendamentoRepository também reclamar do findById, use a busca por Stream que fizemos no Controller:
        Agendamento agendamento = agendamentoRepository.findAll().stream()
                .filter(a -> a.getId().equals(agendamentoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Horário não encontrado!"));

        // 2. Verificar se o horário já está ocupado
        if (agendamento.getBeneficiario() != null) {
            throw new RuntimeException("Este horário já está ocupado por: " +
                    agendamento.getBeneficiario().getNome());
        }

        // 3. Buscar o beneficiário (Agora o findById aceita a String da matrícula perfeitamente!)
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado!"));

        // 4. Realizar o "casamento" (Vincular)
        agendamento.setBeneficiario(beneficiario);

        // 5. Salvar a alteração
        return agendamentoRepository.save(agendamento);
    }
}