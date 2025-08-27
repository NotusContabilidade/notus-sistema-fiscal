package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;

@Entity
@Table(name = "atividade_anexo_map", schema = "simples_nacional")
public class AtividadeAnexoMap {

    public enum AnexoSimples {
        ANEXO_I,
        ANEXO_II,
        ANEXO_III,
        ANEXO_IV,
        ANEXO_V
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código do serviço, ex: "17.02", "01.07". Será nossa chave de busca.
    @Column(name = "codigo_servico", unique = true, nullable = false)
    private String codigoServico;
    
    // Descrição para referência humana
    @Column(name = "descricao_atividade")
    private String descricaoAtividade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnexoSimples anexo;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoServico() { return codigoServico; }
    public void setCodigoServico(String codigoServico) { this.codigoServico = codigoServico; }
    public String getDescricaoAtividade() { return descricaoAtividade; }
    public void setDescricaoAtividade(String descricaoAtividade) { this.descricaoAtividade = descricaoAtividade; }
    public AnexoSimples getAnexo() { return anexo; }
    public void setAnexo(AnexoSimples anexo) { this.anexo = anexo; }
}