package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;

public interface AuthenticationService {

    /**
     * NOVO MÉTODO - Orquestra a criação de um novo tenant e seu primeiro usuário.
     * @param request Os dados de registro do tenant e do primeiro usuário.
     * @return A resposta com o token JWT.
     */
    AuthenticationResponse setupTenantAndFirstUser(RegisterRequest request);
    

    /**
     * Registra um novo usuário para um tenant que já existe.
     * @param request Os dados de registro.
     * @return A resposta com o token JWT.
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Autentica um usuário existente de um tenant específico.
     * @param request Os dados de autenticação (email, senha, tenantId).
     * @return A resposta com o token JWT.
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

}