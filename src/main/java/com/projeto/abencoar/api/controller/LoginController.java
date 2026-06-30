package com.projeto.abencoar.api.controller;

import com.projeto.abencoar.domain.model.PerfilUsuario;
import com.projeto.abencoar.domain.model.Usuario;
import com.projeto.abencoar.domain.service.EspecialidadeService; // 🚀 Garanta que esse import aponta pro seu service correto
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor // 🧠 Traz a injeção automática de dependências pelo construtor (Lombok)
public class LoginController {

    // Injeta o seu serviço para carregar a listagem de especialidades na tela inicial
    private final EspecialidadeService especialidadeService;

    // Usuários cadastrados manualmente para a entrega segura de amanhã
    private final List<Usuario> usuariosValidos = List.of(
            new Usuario("miriam@projeto.com", "abencoar2026", "Miriam Admin", PerfilUsuario.ROLE_ADMIN),
            new Usuario("filipe@ti.com", "tiabencoar", "Filipe Engenheiro", PerfilUsuario.ROLE_TI)
    );

    @GetMapping("/login")
    public String telaLogin() {
        return "login"; // Procura o arquivo login.html
    }

    @PostMapping("/login")
    public String efetuarLogin(
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            HttpSession session,
            Model model) {

        System.out.println("DEBUG: O formulário chegou no Controller! Email: " + email);

        Usuario usuarioEncontrado = usuariosValidos.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()) && u.getSenha().equals(senha))
                .findFirst()
                .orElse(null);

        if (usuarioEncontrado != null) {
            session.setAttribute("usuarioLogado", usuarioEncontrado);

            // 🚀 SALTO TRIPLO PRODUTIVO: Alimentamos o Model com as especialidades que a página inicial exige
            model.addAttribute("especialidades", especialidadeService.listarTodas());

            // Renderiza direto o template HTML, saltando o problema do redirect do navegador!
            return "agenda-projeto";
        }

        model.addAttribute("erro", "❌ Credenciais inválidas para o Projeto Abençoar!");
        return "login";
    }

    @GetMapping("/logout")
    public String efetuarLogout(HttpSession session) {
        session.invalidate(); // Destrói os dados da sessão de segurança
        return "redirect:/login";
    }
}