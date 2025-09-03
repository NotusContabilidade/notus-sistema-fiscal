package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;
import com.notus.contabil.sistema_fiscal.services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/setup")
    public ResponseEntity<AuthenticationResponse> setupTenant(@RequestBody RegisterRequest request) {
        TenantContext.setTenantId(request.getTenantId());
        try {
            AuthenticationResponse response = authenticationService.setupTenantAndFirstUser(request);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        TenantContext.setTenantId(request.getTenantId());
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }
}
