package com.projeto.abencoar.config;

import com.projeto.abencoar.domain.model.*;
import com.projeto.abencoar.domain.repository.AgendamentoRepository;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import com.projeto.abencoar.domain.repository.GradeHorariaRepository;
import com.projeto.abencoar.domain.service.AgendaService;
import com.projeto.abencoar.domain.service.EspecialidadeService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

//@Configuration
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final AgendamentoRepository agendamentoRepository;
    private final EspecialidadeService especialidadeService;
    private final GradeHorariaRepository gradeRepository;
    private final AgendaService agendaService;
    private final BeneficiarioRepository beneficiarioRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Carga do Beneficiário de Teste
        if (beneficiarioRepository.count() == 0) {
            Beneficiario b = new Beneficiario();
            b.setMatricula("MAT000001");
            b.setNome("Filipe Engenheiro");
            b.setCpf(new Cpf("12345678901"));
            b.setTelefone(new Telefone("31999999999"));
            b.setDataNascimento(java.time.LocalDate.of(1995, 5, 6));
            b.setUnidade("Cascata");
            b.setEndereco("Rua do Desenvolvedor, 2026");

            // 🚀 ALINHAMENTO COM O FILTRO: Vincula textualmente à Massoterapia para o painel estratégico encontrar
            b.setNomeResponsavel("Massoterapia");

            beneficiarioRepository.saveAndFlush(b);
        }

        // 2. Carga de Especialidades
        String[] nomesEspecialidades = {
                "Aromoterapia", "Balé", "Beleza", "Cabelereiro",
                "Fisioterapia", "Informática", "Manicure", "Massoterapia",
                "Música", "Neuropsicopedagogia", "Pilates", "Psicologia",
                "Reforço Escolar", "Taekwondo"
        };

        for (String nome : nomesEspecialidades) {
            if (especialidadeService.listarTodas().stream().noneMatch(e -> e.getNome().equalsIgnoreCase(nome))) {
                cadastrarEspecialidade(nome);
            }
        }

        // 3. Configurar Grades Horárias para TODAS as especialidades
        if (gradeRepository.count() <= 1) {
            DayOfWeek[] dias = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
            int diaIndice = 0;

            List<Especialidade> todas = especialidadeService.listarTodas();

            for (Especialidade esp : todas) {
                GradeHoraria regra = new GradeHoraria();
                regra.setEspecialidade(esp);
                regra.setDiaSemana(dias[diaIndice % 5]);
                regra.setPeriodo(Periodo.MANHA);
                regra.setHorarioInicio(LocalTime.of(9, 0));
                regra.setVagasTotais(2);
                regra.setProfissionalPadrao("Prof. " + esp.getNome());

                gradeRepository.save(regra);
                diaIndice++;
            }
            gradeRepository.flush();
            System.out.println(">>> Grades horárias distribuídas com sucesso!");
        }

        // 4. Gerar la Agenda Real para Junho e Julho de 2026
        agendaService.gerarAgendaMensal(6, 2026);
        agendaService.gerarAgendaMensal(7, 2026);
        System.out.println(">>> Processo de carga inicial finalizado para Junho e Julho!");

        // Garante a sincronização total dos dados da agenda gerada antes do vínculo de teste
        entityManager.flush();
        entityManager.clear();

        // 5. Vincular o Filipe ao primeiro horário livre de Massoterapia
        Especialidade masso = especialidadeService.buscarPorNome("Massoterapia");
        if (masso != null) {
            List<Agendamento> horariosLivres = agendamentoRepository.findByEspecialidadeIdAndBeneficiarioIsNull(masso.getId());

            if (!horariosLivres.isEmpty()) {
                Agendamento primeiro = horariosLivres.get(0);
                Beneficiario filipe = beneficiarioRepository.findById("MAT000001").orElse(null);

                if (filipe != null) {
                    primeiro.setBeneficiario(filipe);
                    agendamentoRepository.saveAndFlush(primeiro);
                    System.out.println(">>> SUCESSO COMPROVADO: Filipe Engenheiro agendado para " + primeiro.getData());
                }
            }
        }
    }

    private void cadastrarEspecialidade(String nome) {
        try {
            Especialidade e = new Especialidade();
            e.setNome(nome);
            e.setDescricao("Atendimento de " + nome);
            especialidadeService.salvar(e);
        } catch (Exception ex) {
            // Silencioso se já existir
        }
    }
}