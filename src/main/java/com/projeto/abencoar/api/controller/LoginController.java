package com.projeto.abencoar.api.controller;

import com.projeto.abencoar.domain.model.PerfilUsuario;
import com.projeto.abencoar.domain.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LoginController {

    // Usuários cadastrados manualmente para a entrega segura de amanhã
    private final List<Usuario> usuariosValidos = List.of(
            new Usuario("miriam@projeto.com", "abencoar2026", "Miriam Admin", PerfilUsuario.ROLE_ADMIN),
            new Usuario("filipe@ti.com", "tiabencoar", "Filipe Engenheiro", PerfilUsuario.ROLE_TI)
    );

    @GetMapping("/login")
    public String telaLogin() {
        return "login"; // Vai procurar o arquivo login.html
    }

    @PostMapping("/login")
    public String efetuarLogin(
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            HttpSession session,
            Model model) {

        Usuario usuarioEncontrado = usuariosValidos.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()) && u.getSenha().equals(senha))
                .findFirst()
                .orElse(null);

        if (usuarioEncontrado != null) {
            session.setAttribute("usuarioLogado", usuarioEncontrado);
            return "redirect:/agenda/gestao"; // Logou com sucesso, vai direto pro painel
        }

        model.addAttribute("erro", "❌ Credenciais inválidas para o Projeto Abençoar!");
        return "login";
    }

    @GetMapping("/logout")
    public String efetuarLogout(HttpSession session) {
        session.invalidate(); // Destrói a sessão de segurança
        return "redirect:/login";
    }
}