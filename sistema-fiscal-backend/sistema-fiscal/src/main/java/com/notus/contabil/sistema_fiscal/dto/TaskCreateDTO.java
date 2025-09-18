package com.notus.contabil.sistema_fiscal.dto;

import java.time.LocalDate;
import java.util.List;

public class TaskCreateDTO {
    private String titulo;
    private String descricao;
    private String status;
    private LocalDate prazo;
    private String responsavel;
    private String categoria; // <-- CAMPO ADICIONADO
    private List<String> anexos;
    private Long clienteId;

    // Construtor, Getters e Setters
    public TaskCreateDTO() {}
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getCategoria() { return categoria; } // <-- GETTER ADICIONADO
    public void setCategoria(String categoria) { this.categoria = categoria; } // <-- SETTER ADICIONADO
    public List<String> getAnexos() { return anexos; }
    public void setAnexos(List<String> anexos) { this.anexos = anexos; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}