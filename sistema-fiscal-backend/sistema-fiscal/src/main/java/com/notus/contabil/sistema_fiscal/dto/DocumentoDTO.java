package com.notus.contabil.sistema_fiscal.dto;

import java.time.LocalDateTime;

public class DocumentoDTO {

    private Long id;
    private String nomeArquivo;
    private String tipoDocumento;
    private String status;
    private String comentario;
    private LocalDateTime dataUpload;
    private LocalDateTime dataAprovacao;
    private String usuarioUpload;
    private String usuarioAprovador;
    private Long clienteId;

    public DocumentoDTO() {}

    // Getters e Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }

    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(LocalDateTime dataAprovacao) { this.dataAprovacao = dataAprovacao; }

    public String getUsuarioUpload() { return usuarioUpload; }
    public void setUsuarioUpload(String usuarioUpload) { this.usuarioUpload = usuarioUpload; }

    public String getUsuarioAprovador() { return usuarioAprovador; }
    public void setUsuarioAprovador(String usuarioAprovador) { this.usuarioAprovador = usuarioAprovador; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}
