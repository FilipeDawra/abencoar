package com.projeto.abencoar.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String redirecionarParaAgenda() {
        // Quando alguém acessar o link puro, joga automaticamente para /agenda
        return "redirect:/agenda";
    }
}