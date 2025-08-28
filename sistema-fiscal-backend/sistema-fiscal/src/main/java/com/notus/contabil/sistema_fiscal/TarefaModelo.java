package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidade que representa um "template" de tarefa.
 * É a "receita" para a criação de tarefas recorrentes.
 * Ex: "DEFIS Anual", "DCTF Mensal", etc.
 */
@Entity
@Table(name = "tarefas_modelo")
public class TarefaModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Título da tarefa. Ex: "Apuração do Simples Nacional"
    @Column(nullable = false)
    private String titulo;

    // Instruções ou descrição padrão para quem for executar a tarefa.
    @Column(length = 1000)
    private String descricao;
    
    // Departamento responsável. Ex: "Fiscal", "Contábil", "Pessoal"
    @Column
    private String departamento;

    // --- Lógica de Recorrência ---
    
    // Quantos dias ANTES do vencimento a tarefa deve ser criada? Ex: 15
    @Column(name = "dias_para_criacao_antecipada", nullable = false)
    private int diasParaCriacaoAntecipada;

    // Qual o dia do vencimento? Ex: 20 (para todo dia 20 do mês)
    @Column(name = "dia_vencimento_mes", nullable = false)
    private int diaVencimentoMes;
    
    // Checklist de itens padrão para esta tarefa
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tarefa_modelo_checklist", joinColumns = @JoinColumn(name = "tarefa_modelo_id"))
    @Column(name = "item_checklist")
    private List<String> checklist;

    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getDiasParaCriacaoAntecipada() { return diasParaCriacaoAntecipada; }
    public void setDiasParaCriacaoAntecipada(int diasParaCriacaoAntecipada) { this.diasParaCriacaoAntecipada = diasParaCriacaoAntecipada; }
    public int getDiaVencimentoMes() { return diaVencimentoMes; }
    public void setDiaVencimentoMes(int diaVencimentoMes) { this.diaVencimentoMes = diaVencimentoMes; }
    public List<String> getChecklist() { return checklist; }
    public void setChecklist(List<String> checklist) { this.checklist = checklist; }
}