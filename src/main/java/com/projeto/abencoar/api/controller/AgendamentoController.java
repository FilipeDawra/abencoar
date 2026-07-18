package com.projeto.abencoar.api.controller;

import com.projeto.abencoar.domain.model.*;
import com.projeto.abencoar.domain.repository.AgendamentoRepository;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import com.projeto.abencoar.domain.service.AgendaService;
import com.projeto.abencoar.domain.service.EspecialidadeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final EspecialidadeService BlackespecialidadeService;
    private final AgendaService agendaService;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private com.projeto.abencoar.domain.repository.GradeHorariaRepository gradeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // 1. 🎯 PÁGINA INICIAL ULTRA LIMPA: Apenas a triagem da modalidade
    @GetMapping
    public String exibirFormularioAgendamento(Model model, HttpSession session) {
       /* // Bloqueio de segurança simples
        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }*/
        model.addAttribute("especialidades", BlackespecialidadeService.listarTodas());
        return "agenda-projeto";
    }

    // 2. 📝 DIRECIONAMENTO: Manda os dados iniciais e a lista de profissionais relacionados para as checkboxes
    @PostMapping("/cadastrar-e-agendar")
    public String cadastrarEAgendar(@RequestParam("especialidadeNome") String especialidadeNome, Model model) {
        List<Agendamento> horariosDisponiveis = agendamentoRepository.findAll().stream()
                .filter(a -> a.getBeneficiario() == null && a.getEspecialidade().getNome().equalsIgnoreCase(especialidadeNome))
                .toList();

        // 🚀 CRUCIAL: Extrai os nomes dos profissionais que dão essa aula no momento atual
        List<String> profissionaisRelacionados = agendamentoRepository.findAll().stream()
                .filter(a -> a.getEspecialidade().getNome().equalsIgnoreCase(especialidadeNome)
                        && a.getProfissional() != null
                        && !a.getProfissional().trim().isEmpty()
                        && !a.getProfissional().equalsIgnoreCase("Profissional Clínico")
                        && !a.getProfissional().equalsIgnoreCase("Prof. Grupo"))
                .map(Agendamento::getProfissional)
                .distinct() // Remove nomes duplicados
                .sorted()
                .toList();

        Beneficiario novoBeneficiario = new Beneficiario();
        novoBeneficiario.setCpf(null);
        novoBeneficiario.setTelefone(null);

        model.addAttribute("beneficiario", novoBeneficiario);
        model.addAttribute("horariosDisponiveis", horariosDisponiveis);
        model.addAttribute("especialidadeSelecionada",  especialidadeNome);
        model.addAttribute("profissionaisDisponiveis", profissionaisRelacionados); // 🚀 Injetado para o HTML

        return "beneficiario-form";
    }

    // 3. 🔄 RETORNO AUTOMÁTICO APÓS SALVAR E GERAR CONTRATO (SISTEMA TOTALMENTE DINÂMICO)
    @PostMapping("/salvar-cadastro")
    public String salvarCadastro(
            @RequestParam(value = "profissionalSelecionado", required = false) String profesionalSelecionado,
            @RequestParam(value = "agendamentoId", required = false) String agendamentoIdStr,
            @RequestParam(value = "diasSemanaSelecionados", required = false) List<String> diasSemana,
            @RequestParam(value = "horarioTurma", required = false) String horarioTurma,
            @RequestParam(value = "dataClinica", required = false) String dataClinicaStr,
            @RequestParam(value = "horarioClinica", required = false) String horarioClinicaStr,
            @RequestParam("nome") String nome,
            @RequestParam("dataNascimento") String dataNascimentoStr,
            @RequestParam("cpfInput") String cpfInput,
            @RequestParam("telefoneInput") String telefoneInput,
            @RequestParam("unidade") String unidade,
            @RequestParam("endereco") String endereco,
            @RequestParam(value = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(value = "especialidadeSelecionada", required = false) String espNome,
            RedirectAttributes redirectAttributes) {

        String matriculaParaRedirecionamento = "";

        try {
            String nomeDaBusca = (espNome != null) ? espNome : ((dataClinicaStr != null) ? "Psicologia" : "Pilates");

            Especialidade especialidade = BlackespecialidadeService.listarTodas().stream()
                    .filter(e -> e.getNome().equalsIgnoreCase(nomeDaBusca))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada no sistema: " + nomeDaBusca));

            Beneficiario beneficiario = new Beneficiario();
            beneficiario.setNome(nome);
            beneficiario.setUnidade(unidade);
            beneficiario.setEndereco(endereco);
            beneficiario.setNomeResponsavel(nomeResponsavel);

            String matriculaGerada = "MAT" + (System.currentTimeMillis() % 1000000);
            beneficiario.setMatricula(matriculaGerada);
            matriculaParaRedirecionamento = matriculaGerada;

            if (dataNascimentoStr != null && !dataNascimentoStr.isEmpty()) {
                beneficiario.setDataNascimento(java.time.LocalDate.parse(dataNascimentoStr));
            }

            beneficiario.setCpf(new Cpf(cpfInput));
            beneficiario.setTelefone(new Telefone(telefoneInput));

            // FLUXO A: MUNDO CLÍNICO INDIVIDUAL
            if (dataClinicaStr != null && !dataClinicaStr.isEmpty()) {
                LocalDate dataAtendimento = LocalDate.parse(dataClinicaStr);

                Agendamento agendamentoClinico = new Agendamento();
                agendamentoClinico.setData(dataAtendimento);

                if (horarioClinicaStr != null && !horarioClinicaStr.isEmpty()) {
                    agendamentoClinico.setHorario(LocalTime.parse(horarioClinicaStr));
                }

                agendamentoClinico.setRealizado(false);
                agendamentoClinico.setProfissional(profesionalSelecionado != null && !profesionalSelecionado.isEmpty() ? profesionalSelecionado : "Profissional Clínico");
                agendamentoClinico.setBeneficiario(beneficiario);
                agendamentoClinico.setEspecialidade(especialidade);

                beneficiarioRepository.save(beneficiario);
                agendamentoRepository.save(agendamentoClinico);
            }

            // FLUXO B: MUNDO TURMA EM GRUPO
            else if (diasSemana != null && !diasSemana.isEmpty()) {
                beneficiarioRepository.save(beneficiario);

                for (String dia : diasSemana) {
                    Agendamento agendamentoGrupo = new Agendamento();
                    agendamentoGrupo.setData(LocalDate.now());

                    if (horarioTurma != null && !horarioTurma.isEmpty()) {
                        agendamentoGrupo.setHorario(LocalTime.parse(horarioTurma));
                    }

                    agendamentoGrupo.setRealizado(false);
                    agendamentoGrupo.setProfissional(profesionalSelecionado != null && !profesionalSelecionado.isEmpty() ? profesionalSelecionado : "Prof. Grupo");
                    agendamentoGrupo.setBeneficiario(beneficiario);
                    agendamentoGrupo.setEspecialidade(especialidade);

                    agendamentoRepository.save(agendamentoGrupo);
                }
            }

            // FLUXO C: SISTEMA ANTIGO
            else if (agendamentoIdStr != null && !agendamentoIdStr.trim().isEmpty() && !"null".equals(agendamentoIdStr)) {
                beneficiarioRepository.save(beneficiario);

                Long agendamentoId = Long.parseLong(agendamentoIdStr.trim());
                Agendamento agendamento = agendamentoRepository.findAll().stream()
                        .filter(a -> a.getId().equals(agendamentoId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Horário inexistente: " + agendamentoId));

                agendamento.setBeneficiario(beneficiario);
                agendamentoRepository.save(agendamento);
            }

            else {
                throw new IllegalArgumentException("Nenhum horário, dia da semana ou agendamento foi selecionado.");
            }

            redirectAttributes.addFlashAttribute("mensagemSucesso", "🎉 Beneficiário matriculado e horários dinâmicos reservados com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao processar: " + e.getMessage());
            if (matriculaParaRedirecionamento.isEmpty()) {
                return "redirect:/agenda/gestao";
            }
        }

        return "redirect:/beneficiarios/contrato/" + matriculaParaRedirecionamento;
    }

    // 4. 📋 PAINEL DE GESTÃO ESTRATÉGICA (Filtro via Java Streams - Totalmente Compatível com Postgres/Supabase)
    @GetMapping("/gestao")
    public String exibirGestao(
            @RequestParam(required = false) Long especialidadeId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) StatusInadimplencia inadimplencia,
            @RequestParam(required = false) Integer mesAniversario,
            @RequestParam(required = false) Integer diaAniversario,
            @RequestParam(value = "dataFiltro", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFiltro,
            Model model,
            HttpSession session) { // 🚀 INJETADO AQUI

        /*// 🔒 VERIFICAÇÃO DE PERFIL: Só entra ADMIN ou TI
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) {
            return "redirect:/login";
        }

        // Exemplo de bloqueio por nível: Se quiser blindar algo exclusivo para TI
        // if (logado.getPerfil() != PerfilUsuario.ROLE_TI) { return "redirect:/agenda?erro=nao-autorizado"; }

        model.addAttribute("usuarioNome", logado.getNome()); // Exibe o nome de quem logou na tela*/
        model.addAttribute("especialidades", BlackespecialidadeService.listarTodas());
        model.addAttribute("statusInadimplenciaOpcoes", StatusInadimplencia.values());

        LocalDate dataBusca = (dataFiltro != null) ? dataFiltro : LocalDate.now();
        model.addAttribute("dataSelecionada", dataBusca);
        model.addAttribute("todosBeneficiarios", beneficiarioRepository.findAll());

        String nomeEspecialidadeFiltro = null;
        if (especialidadeId != null) {
            model.addAttribute("especialidadeIdSelecionada", especialidadeId);
            Especialidade esp = BlackespecialidadeService.listarTodas().stream()
                    .filter(e -> e.getId().equals(especialidadeId))
                    .findFirst().orElse(null);

            if (esp != null) {
                model.addAttribute("especialidadeSelecionada", esp);
                nomeEspecialidadeFiltro = esp.getNome();
            }

            List<String> professoresCadastrados = gradeRepository.findAll().stream()
                    .filter(g -> g.getEspecialidade().getId().equals(especialidadeId)
                            && g.getProfissionalPadrao() != null
                            && !g.getProfissionalPadrao().trim().isEmpty()
                            && !g.getProfissionalPadrao().equalsIgnoreCase("Prof. Padrão"))
                    .map(GradeHoraria::getProfissionalPadrao)
                    .distinct()
                    .sorted()
                    .toList();
            model.addAttribute("professoresCadastrados", professoresCadastrados);
        }

        // Substitua a busca engessada por esta lógica flexível:
        if (especialidadeId != null) {
            if (dataFiltro != null) {
                // Se o usuário escolheu um dia específico no calendário, filtra por data
                model.addAttribute("agendamentos", agendamentoRepository.findAllByDataAndEspecialidadeIdOrderByHorarioAsc(dataFiltro, especialidadeId));
            } else {
                // Se não escolheu data, traz todos os horários daquela especialidade independentemente do dia!
                model.addAttribute("agendamentos", agendamentoRepository.findByEspecialidadeIdAndBeneficiarioIsNull(especialidadeId));
            }
        } else {
            model.addAttribute("agendamentos", List.of());
        }

        boolean temBuscaAtiva = (nome != null && !nome.trim().isEmpty())
                || (rua != null && !rua.trim().isEmpty())
                || (inadimplencia != null)
                || (mesAniversario != null && mesAniversario != 0)
                || (diaAniversario != null && diaAniversario != 0)
                || (especialidadeId != null);

        if (temBuscaAtiva) {
            final String espFiltro = nomeEspecialidadeFiltro;

            // 🚀 FILTRO SEGURO EM MEMÓRIA: Evita funções de banco incompatíveis com PostgreSQL
            List<Beneficiario> resultado = beneficiarioRepository.findAll().stream()
                    .filter(b -> {
                        // 🚀 CORREÇÃO DEFINITIVA: Filtra o beneficiário se ele tiver a especialidade em QUALQUER dia ou hora
                        if (espFiltro != null) {
                            boolean temAgendamentoNaEspecialidade = agendamentoRepository.findAll().stream()
                                    .anyMatch(a -> a.getBeneficiario() != null
                                            && a.getBeneficiario().getMatricula().equals(b.getMatricula())
                                            && a.getEspecialidade() != null
                                            && a.getEspecialidade().getNome().trim().equalsIgnoreCase(espFiltro.trim()));

                            if (!temAgendamentoNaEspecialidade) return false;
                        }
                        if (nome != null && !nome.trim().isEmpty()) {
                            if (!b.getNome().toLowerCase().contains(nome.trim().toLowerCase())) return false;
                        }
                        if (rua != null && !rua.trim().isEmpty() && b.getEndereco() != null) {
                            if (!b.getEndereco().toLowerCase().contains(rua.trim().toLowerCase())) return false;
                        }
                        if (inadimplencia != null) {
                            if (b.getStatusInadimplencia() != inadimplencia) return false;
                        }
                        if (mesAniversario != null && mesAniversario != 0 && b.getDataNascimento() != null) {
                            if (b.getDataNascimento().getMonthValue() != mesAniversario) return false;
                        }
                        if (diaAniversario != null && diaAniversario != 0 && b.getDataNascimento() != null) {
                            if (b.getDataNascimento().getDayOfMonth() != diaAniversario) return false;
                        }
                        return true;
                    })
                    .sorted((b1, b2) -> b1.getNome().compareToIgnoreCase(b2.getNome()))
                    .toList();

            System.out.println(">>> [DEBUG GESTÃO] Filtro Especialidade: " + espFiltro);
            System.out.println(">>> [DEBUG GESTÃO] Quantidade de beneficiários encontrados: " + resultado.size());

            model.addAttribute("beneficiarios", resultado);
            model.addAttribute("buscaExecutada", true);
        } else {
            model.addAttribute("beneficiarios", List.of());
            model.addAttribute("buscaExecutada", false);
        }

        model.addAttribute("nomeDigitado", nome);
        model.addAttribute("ruaDigitada", rua);
        model.addAttribute("inadimplenciaSelecionada", inadimplencia);
        model.addAttribute("mesSelecionado", mesAniversario);
        model.addAttribute("diaSelecionado", diaAniversario);

        return "agenda-lista";
    }

    @PostMapping("/atualizar-profissional-slot")
    public String atualizarProfissionalSlot(
            @RequestParam("agendamentoId") Long agendamentoId,
            @RequestParam("novoProfissional") String novoProfissional,
            @RequestParam(value = "especialidadeId", required = false) Long especialidadeId,
            @RequestParam(value = "dataFiltro", required = false) String dataFiltroStr,
            RedirectAttributes redirectAttributes) {
        try {
            Agendamento a = agendamentoRepository.findById(agendamentoId)
                    .orElseThrow(() -> new IllegalArgumentException("Slot não encontrado"));

            a.setProfissional(novoProfissional.trim().isEmpty() ? "Prof. Padrão" : novoProfissional.trim());
            agendamentoRepository.save(a);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "⚡ Profissional updated com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao atualizar profissional: " + e.getMessage());
        }

        String queryParams = "";
        if (especialidadeId != null) queryParams += "especialidadeId=" + especialidadeId;
        if (dataFiltroStr != null && !dataFiltroStr.isEmpty()) {
            queryParams += (queryParams.isEmpty() ? "" : "&") + "dataFiltro=" + dataFiltroStr;
        }

        return "redirect:/agenda/gestao" + (queryParams.isEmpty() ? "" : "?" + queryParams);
    }

    @PostMapping("/cadastrar-profissional-especialidade")
    public String cadastrarProfissionalEspecialidade(
            @RequestParam("especialidadeId") Long especialidadeId,
            @RequestParam("novoProfissional") String novoProfissional,
            @RequestParam(value = "dataFiltro", required = false) String dataFiltroStr,
            RedirectAttributes redirectAttributes) {
        try {
            if (novoProfissional == null || novoProfissional.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do profissional não pode estar vazio.");
            }

            String nomeProf = novoProfissional.trim();

            List<GradeHoraria> grades = gradeRepository.findAll().stream()
                    .filter(g -> g.getEspecialidade().getId().equals(especialidadeId))
                    .toList();

            for (GradeHoraria g : grades) {
                g.setProfissionalPadrao(nomeProf);
                gradeRepository.save(g);
            }

            List<Agendamento> agendamentosFuturos = agendamentoRepository.findAll().stream()
                    .filter(a -> a.getEspecialidade().getId().equals(especialidadeId) && a.getBeneficiario() == null)
                    .toList();

            for (Agendamento a : agendamentosFuturos) {
                a.setProfissional(nomeProf);
                agendamentoRepository.save(a);
            }

            redirectAttributes.addFlashAttribute("mensagemSucesso", "🎉 Profissional '" + nomeProf + "' relacionado com sucesso à modalidade!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao cadastrar profissional: " + e.getMessage());
        }

        return "redirect:/agenda/gestao?especialidadeId=" + especialidadeId + (dataFiltroStr != null && !dataFiltroStr.isEmpty() ? "&dataFiltro=" + dataFiltroStr : "");
    }

    @PostMapping("/vincular-existente")
    public String vincularBeneficiarioExistente(
            @RequestParam("beneficiarioId") String matricula,
            @RequestParam(value = "agendamentoId", required = false) Long agendamentoId,
            @RequestParam(value = "especialidadeNome", required = false) String espNome,
            @RequestParam(value = "diasSemanaSelecionados", required = false) List<String> diasSemana,
            @RequestParam(value = "horarioTurma", required = false) String horarioTurma,
            @RequestParam(value = "dataClinica", required = false) String dataClinicaStr,
            @RequestParam(value = "horarioClinica", required = false) String horarioClinicaStr,
            RedirectAttributes redirectAttributes) {
        try {
            Beneficiario beneficiario = beneficiarioRepository.findById(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com a matrícula: " + matricula));

            if (agendamentoId != null) {
                Agendamento agendamento = agendamentoRepository.findAll().stream()
                        .filter(a -> a.getId().equals(agendamentoId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Slot não localizado. ID: " + agendamentoId));

                agendamento.setBeneficiario(beneficiario);
                agendamentoRepository.save(agendamento);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "🎉 Membro alocado com sucesso!");
            }
            else if (dataClinicaStr != null && !dataClinicaStr.isEmpty()) {
                String nomeDaBusca = (espNome != null && !espNome.isEmpty()) ? espNome : "Psicologia";
                Especialidade especialidade = BlackespecialidadeService.listarTodas().stream()
                        .filter(e -> e.getNome().equalsIgnoreCase(nomeDaBusca)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Especialidade não configurada: " + nomeDaBusca));

                Agendamento agendamentoClinico = new Agendamento();
                agendamentoClinico.setData(LocalDate.parse(dataClinicaStr));
                if (horarioClinicaStr != null && !horarioClinicaStr.isEmpty()) {
                    agendamentoClinico.setHorario(LocalTime.parse(horarioClinicaStr));
                }
                agendamentoClinico.setRealizado(false);
                agendamentoClinico.setProfissional("Profissional Clínico");
                agendamentoClinico.setEspecialidade(especialidade);
                agendamentoClinico.setBeneficiario(beneficiario);

                agendamentoRepository.save(agendamentoClinico);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "🎉 Consulta clínica agendada!");
            }
            else if (diasSemana != null && !diasSemana.isEmpty()) {
                String nomeDaBusca = (espNome != null && !espNome.isEmpty()) ? espNome : "Pilates";
                Especialidade especialidade = BlackespecialidadeService.listarTodas().stream()
                        .filter(e -> e.getNome().equalsIgnoreCase(nomeDaBusca)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Especialidade não configurada: " + nomeDaBusca));

                for (String dia : diasSemana) {
                    Agendamento agendamentoGrupo = new Agendamento();
                    agendamentoGrupo.setData(LocalDate.now());
                    if (horarioTurma != null && !horarioTurma.isEmpty()) {
                        agendamentoGrupo.setHorario(LocalTime.parse(horarioTurma));
                    }
                    agendamentoGrupo.setRealizado(false);
                    agendamentoGrupo.setProfissional("Prof. Grupo");
                    agendamentoGrupo.setEspecialidade(especialidade);
                    agendamentoGrupo.setBeneficiario(beneficiario);

                    agendamentoRepository.save(agendamentoGrupo);
                }
                redirectAttributes.addFlashAttribute("mensagemSucesso", "🎉 Horários de turma criados!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao vincular: " + e.getMessage());
        }
        return "redirect:/agenda/gestao";
    }

    @PostMapping("/gerar-mensal")
    public String gerarAgendaDoMes(
            @RequestParam("especialidadeId") Long especialidadeId,
            @RequestParam("mes") int mes,
            @RequestParam("ano") int ano,
            @RequestParam("horarioFixo") String horarioFixoStr,
            @RequestParam(value = "diasSemana", required = false) List<String> diasSemana,
            @RequestParam(value = "dataEspecifica", required = false) String dataEspecificaStr,
            @RequestParam(value = "dataFiltro", required = false) String dataFiltroStr,
            RedirectAttributes redirectAttributes) {
        try {
            LocalTime horario = LocalTime.parse(horarioFixoStr);

            System.out.println(">>> [GERAÇÃO LOTE] Especialidade ID: " + especialidadeId);
            System.out.println(">>> [GERAÇÃO LOTE] Horário Escolhido: " + horario);
            System.out.println(">>> [GERAÇÃO LOTE] Dias da semana recebidos: " + diasSemana);
            System.out.println(">>> [GERAÇÃO LOTE] Data clínica específica: " + dataEspecificaStr);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "⚡ Parâmetros recebidos no servidor! Prontos para a modelagem do service.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao processar lote: " + e.getMessage());
        }
        return "redirect:/agenda/gestao?especialidadeId=" + especialidadeId + (dataFiltroStr != null && !dataFiltroStr.isEmpty() ? "&dataFiltro=" + dataFiltroStr : "");
    }

    @PostMapping("/excluir-beneficiario")
    public String excluirBeneficiario(@RequestParam("id") String matricula, RedirectAttributes redirectAttributes) {
        try {
            List<Agendamento> agendamentos = agendamentoRepository.findAll();
            for (Agendamento a : agendamentos) {
                if (a.getBeneficiario() != null && matricula.equals(a.getBeneficiario().getMatricula())) {
                    a.setBeneficiario(null);
                    agendamentoRepository.save(a);
                }
            }
            beneficiarioRepository.deleteById(matricula);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "🗑️ Beneficiário excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/agenda/gestao";
    }

    @GetMapping("/imprimir-presenca")
    public String gerarListaPresenca(
            @RequestParam(value = "mapEspecialidadeId", required = false) Long mapEspecialidadeId,
            @RequestParam(value = "especialidadeNome", required = false) String especialidadeNome,
            @RequestParam(value = "dataFiltro", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFiltro,
            Model model) {

        LocalDate dataAlvo = (dataFiltro != null) ? dataFiltro : LocalDate.now();
        model.addAttribute("dataFiltro", dataAlvo);

        String nomeBusca = especialidadeNome;
        if ((nomeBusca == null || nomeBusca.isEmpty()) && mapEspecialidadeId != null) {
            nomeBusca = BlackespecialidadeService.listarTodas().stream()
                    .filter(e -> e.getId().equals(mapEspecialidadeId))
                    .map(Especialidade::getNome)
                    .findFirst().orElse(null);
        }

        if (nomeBusca != null && !nomeBusca.isEmpty()) {
            final String nomeFinal = nomeBusca;
            List<Agendamento> listaPresenca = agendamentoRepository.findAll().stream()
                    .filter(a -> a.getData().equals(dataAlvo)
                            && a.getBeneficiario() != null
                            && a.getEspecialidade().getNome().equalsIgnoreCase(nomeFinal))
                    .toList();

            model.addAttribute("agendamentos", listaPresenca);

            BlackespecialidadeService.listarTodas().stream()
                    .filter(e -> e.getNome().equalsIgnoreCase(nomeFinal))
                    .findFirst().ifPresent(esp -> model.addAttribute("especialidade", esp));
        } else {
            model.addAttribute("agendamentos", List.of());
        }

        return "lista-presenca-impressao";
    }

    @PostMapping("/excluir-profissional-especialidade")
    public String excluirProfissionalEspecialidade(
            @RequestParam("especialidadeId") Long especialidadeId,
            @RequestParam("nomeProfissional") String nomeProfissional,
            @RequestParam(value = "dataFiltro", required = false) String dataFiltroStr,
            RedirectAttributes redirectAttributes) {
        try {
            List<GradeHoraria> grades = gradeRepository.findAll().stream()
                    .filter(g -> g.getEspecialidade().getId().equals(especialidadeId)
                            && g.getProfissionalPadrao() != null
                            && g.getProfissionalPadrao().equalsIgnoreCase(nomeProfissional.trim()))
                    .toList();

            for (GradeHoraria g : grades) {
                g.setProfissionalPadrao("Prof. Padrão");
                gradeRepository.save(g);
            }

            List<Agendamento> slotsVagos = agendamentoRepository.findAll().stream()
                    .filter(a -> a.getEspecialidade().getId().equals(especialidadeId)
                            && a.getBeneficiario() == null
                            && a.getProfissional() != null
                            && a.getProfissional().equalsIgnoreCase(nomeProfissional.trim()))
                    .toList();

            for (Agendamento a : slotsVagos) {
                a.setProfissional("Prof. Padrão");
                agendamentoRepository.save(a);
            }

            redirectAttributes.addFlashAttribute("mensagemSucesso", "🗑️ Profissional '" + nomeProfissional + "' removido da modalidade com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao remover profissional: " + e.getMessage());
        }

        return "redirect:/agenda/gestao?especialidadeId=" + especialidadeId + (dataFiltroStr != null && !dataFiltroStr.isEmpty() ? "&dataFiltro=" + dataFiltroStr : "");
    }
}