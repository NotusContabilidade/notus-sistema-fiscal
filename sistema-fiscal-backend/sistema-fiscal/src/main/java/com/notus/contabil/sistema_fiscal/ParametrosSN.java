package com.notus.contabil.sistema_fiscal;
import jakarta.persistence.*;
@Entity
@Table(name = "parametros_sn", schema = "simples_nacional")
public class ParametrosSN {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Column(name = "rbt12_atual", nullable = false) private Double rbt12Atual;
    @Column(name = "folha_pagamento_12m_atual", nullable = false) private Double folhaPagamento12mAtual;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Double getRbt12Atual() { return rbt12Atual; }
    public void setRbt12Atual(Double rbt12Atual) { this.rbt12Atual = rbt12Atual; }
    public Double getFolhaPagamento12mAtual() { return folhaPagamento12mAtual; }
    public void setFolhaPagamento12mAtual(Double folhaPagamento12mAtual) { this.folhaPagamento12mAtual = folhaPagamento12mAtual; }
}