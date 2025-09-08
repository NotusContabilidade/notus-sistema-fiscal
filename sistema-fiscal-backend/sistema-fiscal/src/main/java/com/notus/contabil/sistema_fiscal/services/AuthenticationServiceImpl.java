package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;
import com.notus.contabil.sistema_fiscal.entity.User;
import com.notus.contabil.sistema_fiscal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private TenantManagementService tenantManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationResponse setupTenantAndFirstUser(RegisterRequest request) {
        String tenantId = request.getTenantId().toLowerCase();

        try {
            // Cria o schema e as tabelas do tenant
            tenantManagementService.criarTenant(tenantId);

            // Ativa o contexto do tenant antes de salvar o usuário
            TenantContext.setTenantId(tenantId);

            // Cria o usuário no schema do tenant
            User user = new User();
            user.setNome(request.getNome());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole().toString());
            user.setTenantId(tenantId);

            userRepository.save(user);

            // Retorna resposta (ajuste conforme seu fluxo)
            AuthenticationResponse response = new AuthenticationResponse();
            response.setToken("token_exemplo");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar tenant: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        String tenantId = request.getTenantId().toLowerCase();
        // Lógica para registrar usuário em tenant já existente
        User user = new User();
        user.setNome(request.getNome());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toString());
        user.setTenantId(request.getTenantId());

        userRepository.save(user);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("token_exemplo"); // Substitua pela lógica real de geração de token
        return response;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Lógica para autenticar usuário
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Aqui você pode validar a senha e gerar o token JWT
        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("token_exemplo"); // Substitua pela lógica real de geração de token
        return response;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}