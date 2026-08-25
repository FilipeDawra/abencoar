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
        String matriculaBusca = (id != null) ? id.trim() : "";

        Beneficiario beneficiario = beneficiarioRepository.findById(matriculaBusca)
                .orElse(null);

        if (beneficiario == null) {
            return "redirect:/beneficiarios/gestao?erro=nao_encontrado";
        }

        // 🛡️ PROTEÇÃO ESSENCIAL: Evita que o Thymeleaf dê erro se o telefone estiver nulo no banco
        if (beneficiario.getTelefone() == null) {
            beneficiario.setTelefone(new Telefone(""));
        }

        model.addAttribute("beneficiario", beneficiario);
        return "beneficiario-form";
    }

    // 💾 2. SALVAR/ATUALIZAR CADASTRO (Com Higienização, Log e Redirecionamento Protegido)
    @PostMapping("/salvar-detalhes")
    public String salvar(@ModelAttribute("beneficiario") Beneficiario b) {
        // 🧹 HIGIENIZAÇÃO DE DADOS (Sanitization) antes de persistir
        if (b.getCpf() != null && b.getCpf().getNumero() != null) {
            String cpfLimpo = b.getCpf().getNumero().replaceAll("\\D", "");
            b.setCpf(new Cpf(cpfLimpo));
        }

        if (b.getTelefone() != null && b.getTelefone().getNumero() != null) {
            String telefoneLimpo = b.getTelefone().getNumero().replaceAll("\\D", "");
            b.setTelefone(new Telefone(telefoneLimpo));
        }

        // 🔍 LOGS DE CONTROLE
        System.out.println("=========================================");
        System.out.println(">>> RECEBIDO DO FORMULÁRIO:");
        System.out.println(">>> Matrícula: " + b.getMatricula());
        System.out.println(">>> Nome: " + b.getNome());
        System.out.println("=========================================");

        // Salva ou atualiza no banco
        Beneficiario salvo = beneficiarioRepository.save(b);

        if (salvo.getMatricula() == null || salvo.getMatricula().isBlank()) {
            System.out.println("⚠️ ALERTA: Matrícula retornou nula após o save. Redirecionando para a gestão.");
            return "redirect:/beneficiarios/gestao?sucesso=true";
        }

        String matriculaLimpa = salvo.getMatricula().trim();
        System.out.println("🚀 SUCESSO: Redirecionando para o contrato da matrícula: " + matriculaLimpa);
        return "redirect:/beneficiarios/contrato/" + matriculaLimpa;
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

    // 📄 6. GERADOR DE CONTRATO EM A4 (Tratado contra erros e exceções)
    @GetMapping("/contrato/{id}")
    public String gerarContrato(@PathVariable("id") String id, Model model) {

        // 🧹 1. Remove espaços invisíveis nas pontas da matrícula (ex: "MAT66843 ")
        String matriculaBusca = (id != null) ? id.trim() : "";

        // 🔍 2. Busca usando a variável LIMPA e retorna null em vez de estourar Exceção
        Beneficiario beneficiario = beneficiarioRepository.findById(matriculaBusca)
                .orElse(null);

        // 🛡️ 3. Se o banco não encontrar o registro, redireciona em segurança
        if (beneficiario == null) {
            System.out.println("⚠️ ALERTA: Beneficiário com matrícula [" + matriculaBusca + "] não foi localizado no banco.");
            return "redirect:/beneficiarios/gestao?erro=nao_encontrado";
        }

        // 🛡️ 4. Blindagens contra atributos nulos
        if (beneficiario.getTelefone() == null) {
            beneficiario.setTelefone(new Telefone(""));
        }

        if (beneficiario.getCpf() == null) {
            beneficiario.setCpf(new Cpf(""));
        }

        if (beneficiario.getDataNascimento() == null) {
            beneficiario.setDataNascimento(java.time.LocalDate.now());
        }

        model.addAttribute("b", beneficiario);

        return "beneficiario-contrato-impressao";
    }

    // 🖨️ 7. GERADOR DE LISTA DE PRESENÇA / CHAMADA POR ESPECIALIDADE
    @GetMapping("/impressao-chamada")
    public String imprimirListaChamada(
            @RequestParam(value = "especialidade", required = false) String especialidade,
            @RequestParam(value = "unidade", required = false, defaultValue = "Cascata") String unidade,
            Model model) {

        String especialidadeBusca = (especialidade != null && !especialidade.isBlank()) ? especialidade.trim() : null;

        // Busca os beneficiários aplicando o filtro de especialidade
        List<Beneficiario> listaBeneficiarios = beneficiarioRepository.buscarBeneficiariosComFiltrosAvancados(
                null, // nome
                null, // rua
                null, // status
                null, // mesAniversario
                null, // diaAniversario
                especialidadeBusca
        );

        // Preenche as variáveis que o HTML da lista de chamada exige
        model.addAttribute("agendamentos", listaBeneficiarios); // <--- Casando com o th:each="${agendamentos}"
        model.addAttribute("especialidade", especialidadeBusca != null ? especialidadeBusca : "Todas");
        model.addAttribute("unidade", unidade);

        return "lista-de-chamada"; // Nome exato do seu arquivo HTML de impressão
    }
}