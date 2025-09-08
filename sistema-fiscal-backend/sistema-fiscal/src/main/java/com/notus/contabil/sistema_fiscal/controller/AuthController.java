package com.notus.contabil.sistema_fiscal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;
import com.notus.contabil.sistema_fiscal.services.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/setup")
    public ResponseEntity<AuthenticationResponse> setupTenant(@RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.setupTenantAndFirstUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        TenantContext.setTenantId(request.getTenantId().toLowerCase());
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }
}
