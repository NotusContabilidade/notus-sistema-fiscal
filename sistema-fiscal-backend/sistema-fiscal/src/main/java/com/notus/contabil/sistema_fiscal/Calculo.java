package com.notus.contabil.sistema_fiscal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "calculos") // A referência ao schema foi removida daqui também
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
    
    @Column(name = "data_calculo", updatable = false)
    private LocalDateTime dataCalculo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes_json", columnDefinition = "jsonb")
    private String detalhesJson;

    @Transient
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PrePersist
    protected void onCreate() {
        dataCalculo = LocalDateTime.now();
    }

    public List<ResultadoCalculoDetalhado> getDetalhes() throws JsonProcessingException {
        if (this.detalhesJson == null || this.detalhesJson.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(this.detalhesJson, new TypeReference<>() {});
    }

    public String getDataCalculoFormatada() {
        if (this.dataCalculo == null) return "N/D";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return this.dataCalculo.format(formatter);
    }
    
    // Getters e Setters (sem os de tenantId)
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
    public LocalDateTime getDataCalculo() { return dataCalculo; }
    public void setDataCalculo(LocalDateTime dataCalculo) { this.dataCalculo = dataCalculo; }
    public String getDetalhesJson() { return detalhesJson; }
    public void setDetalhesJson(String detalhesJson) { this.detalhesJson = detalhesJson; }
}