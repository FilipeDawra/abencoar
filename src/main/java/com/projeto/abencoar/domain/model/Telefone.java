package com.projeto.abencoar.domain.model;

import jakarta.persistence.Embeddable;// Diz ao JPA para salvar os campos deste objeto na tabela do Beneficiario
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // Diz ao JPA para salvar os campos deste objeto na tabela do Beneficiario
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Telefone {

    private String numero;

    public Telefone(String numero) {
        if (numero == null || !numero.matches("^\\(?[1-9]{2}\\)?\\s?9[0-9]{4}-?[0-9]{4}$")) {
            throw new IllegalArgumentException("Telefone inválido: deve conter 11 dígitos numéricos.");
        }
        this.numero = numero.replaceAll("\\D", "");
    }
}