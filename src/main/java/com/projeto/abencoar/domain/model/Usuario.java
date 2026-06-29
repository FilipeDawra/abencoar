package com.projeto.abencoar.domain.model;

import lombok.Data;

@Data
public class Usuario {
    private String email;
    private String senha;
    private String nome;
    private PerfilUsuario perfil;

    public Usuario(String email, String senha, String nome, PerfilUsuario perfil) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.perfil = perfil;
    }
}