package com.projeto.abencoar.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zab_beneficiarios")
@Getter @Setter @NoArgsConstructor
public class Beneficiario {

    // 🚀 ALTERAÇÃO CRUCIAL: Mudamos de 'id (Long)' para 'matricula (String)'
    // para casar 100% com o seu banco do Postgres. Removemos também o GenerationType
    // já que as matrículas vêm prontas do CSV ou são geradas de forma textual.
    @Id
    @Column(name = "matricula", length = 10)
    private String matricula;

    private String nome;

    @Embedded
    @AttributeOverride(name = "numero", column = @Column(name = "cpf"))
    private Cpf cpf;

    @Embedded
    @AttributeOverride(name = "numero", column = @Column(name = "telefone"))
    private Telefone telefone;

    private String unidade; // Jardim das Rosas ou Cascata

    private String email;

    @Column(name = "data_nascimento")
    private java.time.LocalDate dataNascimento;

    private String endereco;

    @Enumerated(EnumType.STRING)
    private StatusBeneficiario status = StatusBeneficiario.ATIVO;

    private String nomeResponsavel;

    @Enumerated(EnumType.STRING)
    private StatusInadimplencia statusInadimplencia = StatusInadimplencia.ADIMPLENTE;
}