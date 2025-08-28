package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma instância de tarefa, gerada a partir de um TarefaModelo.
 * É a tarefa real que um contador irá executar para um cliente específico.
 */
@Entity
@Table(name = "tarefas_reais")
public class TarefaReal {

    public enum StatusTarefa {
        PENDENTE,
        EM_ANDAMENTO,
        CONCLUIDA,
        ATRASADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link para o modelo que originou esta tarefa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_modelo_id")
    private TarefaModelo tarefaModelo;

    // Link para o cliente específico desta tarefa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Link para o usuário responsável (contador)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario responsavel;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column
    private LocalDateTime dataConclusao;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status;

    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TarefaModelo getTarefaModelo() { return tarefaModelo; }
    public void setTarefaModelo(TarefaModelo tarefaModelo) { this.tarefaModelo = tarefaModelo; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
}