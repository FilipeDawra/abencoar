package com.projeto.abencoar.domain.model;

import lombok.Getter;

@Getter
public enum StatusBeneficiario {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    PENDENTE("Pendente");

    private final String descricao;

    StatusBeneficiario(String descricao) {
        this.descricao = descricao;
    }
}

