package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;

@Entity
// ✅ ALTERAÇÃO: A anotação de schema foi removida.
@Table(name = "atividade_anexo_map")
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

    @Column(name = "codigo_servico", unique = true, nullable = false)
    private String codigoServico;
    
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