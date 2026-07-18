package com.projeto.abencoar;

import com.projeto.abencoar.domain.model.Beneficiario;
import com.projeto.abencoar.domain.model.Cpf;
import com.projeto.abencoar.domain.model.Telefone;
import com.projeto.abencoar.domain.model.Especialidade;
import com.projeto.abencoar.domain.model.GradeHoraria;
import com.projeto.abencoar.domain.model.Periodo;
import com.projeto.abencoar.domain.model.Agendamento;
import com.projeto.abencoar.domain.repository.AgendamentoRepository;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import com.projeto.abencoar.domain.repository.GradeHorariaRepository;
import com.projeto.abencoar.domain.service.AgendaService;
import com.projeto.abencoar.domain.service.EspecialidadeService;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class AbencoarApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbencoarApplication.class, args);
	}

	@Bean
	@Transactional
	public CommandLineRunner carregarDadosIniciais(
			AgendamentoRepository agendamentoRepository,
			EspecialidadeService especialidadeService,
			GradeHorariaRepository gradeRepository,
			AgendaService agendaService,
			BeneficiarioRepository beneficiarioRepository,
			EntityManager entityManager) {

		return args -> {
			System.out.println("====== [DEBUG] EXECUÇÃO DA CARGA DE DADOS INICIAIS INICIADA ======");

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
				b.setNomeResponsavel("Massoterapia");

				beneficiarioRepository.saveAndFlush(b);
				System.out.println(">>> Beneficiário de teste cadastrado.");
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
					Especialidade e = new Especialidade();
					e.setNome(nome);
					e.setDescricao("Atendimento de " + nome);
					especialidadeService.salvar(e);
				}
			}
			System.out.println(">>> Especialidades verificadas/cadastradas.");

			// 3. Configurar Grades Horárias
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

			// 4. Gerar a Agenda Real para Junho e Julho de 2026
			agendaService.gerarAgendaMensal(6, 2026);
			agendaService.gerarAgendaMensal(7, 2026);
			System.out.println(">>> Agenda gerada com sucesso!");

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
						// 🚀 Trocado de saveAndFlush para save comum
						agendamentoRepository.save(primeiro);
						System.out.println(">>> SUCESSO COMPROVADO: Filipe Engenheiro agendado para " + primeiro.getData());
					}
				}
			}
			System.out.println("====== [DEBUG] FIM DA CARGA DE DADOS ======");
		};
	}
}