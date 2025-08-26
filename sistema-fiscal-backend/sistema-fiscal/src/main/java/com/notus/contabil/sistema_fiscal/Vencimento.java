package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vencimentos", schema = "simples_nacional")
public class Vencimento {

    // Usar um Enum para status é uma boa prática, evita erros de digitação.
    public enum StatusVencimento {
        PENDENTE,
        PAGO,
        ATRASADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos vencimentos podem pertencer a um cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String descricao; // Ex: "DAS Mensal", "DEFIS Anual"

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING) // Grava o nome do status ("PENDENTE") no banco, mais legível
    @Column(nullable = false)
    private StatusVencimento status;

    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public StatusVencimento getStatus() { return status; }
    public void setStatus(StatusVencimento status) { this.status = status; }
}