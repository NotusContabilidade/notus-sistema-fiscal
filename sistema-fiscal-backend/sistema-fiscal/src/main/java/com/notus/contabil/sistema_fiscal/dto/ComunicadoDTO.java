package com.notus.contabil.sistema_fiscal.dto;

import java.time.LocalDateTime;

public class ComunicadoDTO {
    private Long id;
    private String titulo;
    private String mensagem;
    private LocalDateTime dataCriacao;
    private Long clienteId;
    private String clienteRazaoSocial;

    // Construtor padrão
    public ComunicadoDTO() {
    }

    // Construtor com todos os campos
    public ComunicadoDTO(Long id, String titulo, String mensagem, LocalDateTime dataCriacao, Long clienteId, String clienteRazaoSocial) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.dataCriacao = dataCriacao;
        this.clienteId = clienteId;
        this.clienteRazaoSocial = clienteRazaoSocial;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteRazaoSocial() {
        return clienteRazaoSocial;
    }

    public void setClienteRazaoSocial(String clienteRazaoSocial) {
        this.clienteRazaoSocial = clienteRazaoSocial;
    }
}