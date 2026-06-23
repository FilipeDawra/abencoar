package com.projeto.abencoar.domain.model;

import jakarta.persistence.Embeddable;// Diz ao JPA para salvar os campos deste objeto na tabela do Beneficiario
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // Diz ao JPA para salvar os campos deste objeto na tabela do Beneficiario
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cpf {

    private String numero;

    public Cpf(String numero) {
        if (numero == null || !numero.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido: deve conter 11 dígitos numéricos.");
        }
        this.numero = numero;
    }
}