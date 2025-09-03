package com.notus.contabil.sistema_fiscal.dto.auth;

import com.notus.contabil.sistema_fiscal.entity.Role;

public class RegisterRequest {

    private String nome;
    private String email;
    private String password;
    private String tenantId;
    private Role role;

    public RegisterRequest() {
    }

    public RegisterRequest(String nome, String email, String password, String tenantId, Role role) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
        this.role = role;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}