package com.projeto.abencoar.api.controller;

import com.projeto.abencoar.domain.model.Beneficiario;
import com.projeto.abencoar.domain.model.Cpf;
import com.projeto.abencoar.domain.model.StatusInadimplencia;
import com.projeto.abencoar.domain.model.Telefone;
import com.projeto.abencoar.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/beneficiarios")
@RequiredArgsConstructor
public class BeneficiarioController {

    private final BeneficiarioRepository beneficiarioRepository;

    // ✏️ 1. ROTA EDITAR FICHA (Corrigido para String/Matrícula)
    @GetMapping("/editar/{id}")
    public String editarBeneficiario(@PathVariable String id, Model model) {
        // Agora o findById aceita a String da matrícula perfeitamente!
        Beneficiario beneficiario = beneficiarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiário inválido: " + id));

        // 🛡️ PROTEÇÃO ESSENCIAL: Evita que o Thymeleaf dê erro se o telefone estiver nulo no banco
        if (beneficiario.getTelefone() == null) {
            beneficiario.setTelefone(new Telefone());
        }

        model.addAttribute("beneficiario", beneficiario);
        return "beneficiario-form";
    }

    // 💾 2. SALVAR/ATUALIZAR CADASTRO (Com Log de Controle e Redirecionamento Protegido)
    @PostMapping("/salvar-detalhes")
    public String salvar(@ModelAttribute("beneficiario") Beneficiario b) {
        // 🔍 LOGS DE CONTROLE - Verifique o console do IntelliJ ao clicar em Salvar
        System.out.println("=========================================");
        System.out.println(">>> RECEBIDO DO FORMULÁRIO:");
        System.out.println(">>> Matrícula: " + b.getMatricula());
        System.out.println(">>> Nome: " + b.getNome());
        System.out.println("=========================================");

        // O Spring JPA salva ou atualiza o beneficiário usando a matrícula textual
        Beneficiario salvo = beneficiarioRepository.save(b);

        // 🛡️ PROTEÇÃO DE FLUXO: Se a matrícula veio vazia ou nula por erro do HTML,
        // redireciona para a listagem para não quebrar a aplicação com erro 500 ou 404
        if (salvo.getMatricula() == null || salvo.getMatricula().isBlank()) {
            System.out.println("⚠️ ALERTA: Matrícula retornou nula após o save. Redirecionando para a gestão.");
            return "redirect:/beneficiarios/gestao?sucesso=true";
        }

        // Se a matrícula estiver perfeita, exibe o contrato na hora!
        System.out.println("🚀 SUCESSO: Redirecionando para o contrato da matrícula: " + salvo.getMatricula());
        return "redirect:/beneficiarios/contrato/" + salvo.getMatricula();
    }

    // 🔍 3. Rota para o Autocomplete de Nome
    @GetMapping("/autocomplete-nome")
    @ResponseBody
    public List<String> autocompleteNome(@RequestParam("term") String termo) {
        List<Beneficiario> beneficiarios = beneficiarioRepository.findByNomeContainingIgnoreCase(termo);

        return beneficiarios.stream()
                .map(Beneficiario::getNome)
                .distinct()
                .limit(10)
                .toList();
    }

    // 📍 4. Rota para o Autocomplete de Rua
    @GetMapping("/autocomplete-rua")
    @ResponseBody
    public List<String> autocompleteRua(@RequestParam("term") String termo) {
        List<Beneficiario> beneficiarios = beneficiarioRepository.findByEnderecoContainingIgnoreCase(termo);

        return beneficiarios.stream()
                .map(Beneficiario::getEndereco)
                .filter(end -> end != null && !end.isBlank())
                .distinct()
                .limit(10)
                .toList();
    }

    // 📊 5. ROTA DE GESTÃO / LISTAGEM DE BENEFICIÁRIOS
    @GetMapping("/gestao")
    public String listarBeneficiarios(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "rua", required = false) String rua,
            @RequestParam(value = "mesAniversario", required = false) Integer mesAniversario,
            @RequestParam(value = "status", required = false) StatusInadimplencia status,
            Model model) {

        String nomeBusca = (nome != null && !nome.isEmpty()) ? nome : null;
        String ruaBusca = (rua != null && !rua.isEmpty()) ? rua : null;

        List<Beneficiario> lista = beneficiarioRepository.buscarBeneficiariosComFiltrosAvancados(
                nomeBusca,
                ruaBusca,
                status,
                mesAniversario,
                null,
                null
        );

        model.addAttribute("beneficiarios", lista);
        model.addAttribute("nomeFiltro", nome);
        model.addAttribute("ruaFiltro", rua);
        model.addAttribute("mesFiltro", mesAniversario);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("statusLista", StatusInadimplencia.values());

        return "beneficiarios-lista";
    }

    // 📄 6. GERADOR DE CONTRATO EM A4 (Corrigido para String/Matrícula e Blindado contra Nulos)
    @GetMapping("/contrato/{id}")
    public String gerarContrato(@PathVariable("id") String id, Model model) {
        Beneficiario beneficiario = beneficiarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiário inválido: " + id));

        // 🛡️ SEGURO PARA TESTES: Passando string vazia para os construtores que exigem argumentos
        if (beneficiario.getTelefone() == null) {
            beneficiario.setTelefone(new Telefone(""));
        }

        if (beneficiario.getCpf() == null) {
            beneficiario.setCpf(new Cpf(""));
        }

        // 🛡️ SEGURO PARA TESTES: Se a data de nascimento for nula, define a de hoje para não quebrar a formatação do Thymeleaf
        if (beneficiario.getDataNascimento() == null) {
            beneficiario.setDataNascimento(java.time.LocalDate.now());
        }

        model.addAttribute("b", beneficiario);

        return "beneficiario-contrato-impressao";
    }
}