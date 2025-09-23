package com.notus.contabil.sistema_fiscal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob; // <-- PODE REMOVER ESTE IMPORT
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @Column(name = "status", nullable = false)
    private String status; // PENDENTE, APROVADO, REJEITADO

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "usuario_upload", nullable = false)
    private String usuarioUpload;

    @Column(name = "usuario_aprovador")
    private String usuarioAprovador;

    // @Lob // <-- REMOVIDO
    // @Column(name = "conteudo", nullable = false) // <-- REMOVIDO
    // private byte[] conteudo; // <-- REMOVIDO

    @Column(name = "storage_key") // <-- ADICIONADO
    private String storageKey;    // <-- ADICIONADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    public Documento() {}

    // Getters e Setters (conteudo foi removido e storageKey foi adicionado)

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

    // public byte[] getConteudo() { return conteudo; } // <-- REMOVIDO
    // public void setConteudo(byte[] conteudo) { this.conteudo = conteudo; } // <-- REMOVIDO

    public String getStorageKey() { return storageKey; } // <-- ADICIONADO
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; } // <-- ADICIONADO

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}