package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.services.TenantManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de uso administrativo.
 * Permite criar schemas "na mão" sem cadastrar escritório/usuário.
 * 
 * Obs: no fluxo normal, use apenas o endpoint /api/auth/setup.
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantManagementService tenantManagementService;

    public TenantController(TenantManagementService tenantManagementService) {
        this.tenantManagementService = tenantManagementService;
    }

    /**
     * Cria apenas o schema vazio do tenant (sem registrar escritório/usuário).
     * Útil para testes ou administração manual.
     */
    @PostMapping("/{tenantId}")
    public ResponseEntity<String> criarTenant(@PathVariable String tenantId) {
        try {
            tenantManagementService.criarTenant(tenantId);
            return ResponseEntity.ok("Schema do tenant '" + tenantId + "' criado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar tenant: " + e.getMessage());
        }
    }
}
