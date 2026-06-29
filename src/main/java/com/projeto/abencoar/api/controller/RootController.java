package com.projeto.abencoar.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String redirecionarParaLogin() {
        // Quando o Joel ou a Miriam digitarem o link limpo, caem direto no Login
        return "redirect:/login";
    }
}