package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.*;

@Entity
// ✅ ALTERAÇÃO: A anotação de schema foi removida.
@Table(name = "clientes")
public class Cliente {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    
    @Column(nullable = false, unique = true, length = 14) 
    private String cnpj;

    @Column(name = "razao_social", nullable = false) 
    private String razaoSocial;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
}