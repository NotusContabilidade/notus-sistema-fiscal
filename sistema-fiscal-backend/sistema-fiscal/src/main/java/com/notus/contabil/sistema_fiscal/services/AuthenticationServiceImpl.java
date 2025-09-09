package com.notus.contabil.sistema_fiscal.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.config.security.JwtService;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;
import com.notus.contabil.sistema_fiscal.entity.User;
import com.notus.contabil.sistema_fiscal.repository.UserRepository;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private TenantManagementService tenantManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

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

            // Gera JWT real
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
            );
            String jwt = jwtService.generateToken(userDetails, tenantId);

            AuthenticationResponse response = new AuthenticationResponse();
            response.setToken(jwt);
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
        user.setTenantId(tenantId);

        userRepository.save(user);

        // Gera JWT real
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
        String jwt = jwtService.generateToken(userDetails, tenantId);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken(jwt);
        return response;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Lógica para autenticar usuário
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Valida a senha
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Senha inválida");
        }

        // Gera JWT real
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
        String jwt = jwtService.generateToken(userDetails, user.getTenantId());

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken(jwt);
        return response;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         System.out.println("Buscando usuário: " + username + " no tenant: " + TenantContext.getTenantId());
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}