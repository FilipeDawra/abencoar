package com.projeto.abencoar.api.controller;

import com.projeto.abencoar.domain.model.GradeHoraria;
import com.projeto.abencoar.domain.repository.EspecialidadeRepository;
import com.projeto.abencoar.domain.repository.GradeHorariaRepository;
import com.projeto.abencoar.domain.service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/admin/grades")
@RequiredArgsConstructor
public class GradeHorariaController {

    private final GradeHorariaRepository gradeRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final AgendaService agendaService;

    // Tela que lista as grades atuais e tem o formulário para criar uma nova
    @GetMapping
    public String formularioGrade(Model model) {
        model.addAttribute("grades", gradeRepository.findAll());
        model.addAttribute("especialidades", especialidadeRepository.findAll());
        model.addAttribute("diasSemana", DayOfWeek.values());
        model.addAttribute("novaGrade", new GradeHoraria());
        return "admin-grades"; // Nome do HTML que faremos
    }

    // Salva a nova grade que o Bil inventar
    @PostMapping("/salvar")
    public String salvarGrade(@ModelAttribute("novaGrade") GradeHoraria grade) {
        gradeRepository.save(grade);
        return "redirect:/admin/grades?sucesso=cadastro";
    }

    // O Botão Mágico que o Bil clica para rodar o motor do AgendaService
    @PostMapping("/gerar-agenda")
    public String gerarAgendaDoMes(@RequestParam("mes") int mes, @RequestParam("ano") int ano) {
        agendaService.gerarAgendaMensal(mes, ano);
        return "redirect:/admin/grades?sucesso=gerado";
    }
}