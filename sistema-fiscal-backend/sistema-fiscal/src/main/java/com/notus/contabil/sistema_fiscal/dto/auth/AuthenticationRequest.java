package com.notus.contabil.sistema_fiscal.dto.auth;

public class AuthenticationRequest {

    private String email;
    private String password;
    private String tenantId;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(String email, String password, String tenantId) {
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}