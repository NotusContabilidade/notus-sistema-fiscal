package com.notus.contabil.sistema_fiscal;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes; // Import correto do Hibernate

import com.fasterxml.jackson.core.JsonProcessingException;          // Import correto do Hibernate
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@Entity
@Table(name = "calculos", schema = "simples_nacional")
public class Calculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "mes_referencia", nullable = false)
    private Integer mesReferencia;

    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;

    @Column(name = "das_total", nullable = false)
    private Double dasTotal;
    
    @Column(name = "data_calculo", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp dataCalculo;

    // Anotação nativa do Hibernate para JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes_json", columnDefinition = "jsonb")
    private String detalhesJson;

    @Transient
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ResultadoCalculoDetalhado> getDetalhes() throws JsonProcessingException {
        if (this.detalhesJson == null || this.detalhesJson.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(this.detalhesJson, new TypeReference<>() {});
    }

    public String getDataCalculoFormatada() {
        if (this.dataCalculo == null) return "N/D";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this.dataCalculo);
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Integer getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(Integer mesReferencia) { this.mesReferencia = mesReferencia; }
    public Integer getAnoReferencia() { return anoReferencia; }
    public void setAnoReferencia(Integer anoReferencia) { this.anoReferencia = anoReferencia; }
    public Double getDasTotal() { return dasTotal; }
    public void setDasTotal(Double dasTotal) { this.dasTotal = dasTotal; }
    public Timestamp getDataCalculo() { return dataCalculo; }
    public void setDataCalculo(Timestamp dataCalculo) { this.dataCalculo = dataCalculo; }
    public String getDetalhesJson() { return detalhesJson; }
    public void setDetalhesJson(String detalhesJson) { this.detalhesJson = detalhesJson; }
}