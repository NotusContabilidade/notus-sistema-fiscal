package com.notus.contabil.sistema_fiscal.services;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importe Transactional

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.config.security.JwtService;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationRequest;
import com.notus.contabil.sistema_fiscal.dto.auth.AuthenticationResponse;
import com.notus.contabil.sistema_fiscal.dto.auth.RegisterRequest;
import com.notus.contabil.sistema_fiscal.entity.Office; // Importe Office
import com.notus.contabil.sistema_fiscal.entity.User;
import com.notus.contabil.sistema_fiscal.repository.OfficeRepository; // Importe OfficeRepository
import com.notus.contabil.sistema_fiscal.repository.UserRepository;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired private TenantManagementService tenantManagementService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private OfficeRepository officeRepository; // <-- DEPENDÊNCIA INJETADA

    @Override
    @Transactional // Garante que toda a operação (criação de schema, registro de office e usuário) seja uma única transação atômica.
    public AuthenticationResponse setupTenantAndFirstUser(RegisterRequest request) {
        String tenantId = request.getTenantId().toLowerCase();

        try {
            // 1. Cria o schema e as tabelas do tenant no banco de dados.
            tenantManagementService.criarTenant(tenantId);

            // 2. <-- INÍCIO DA NOVA IMPLEMENTAÇÃO -->
            // Registra o novo tenant na tabela de controle 'public.offices' para que o sistema saiba que ele existe.
            if (officeRepository.findByName(tenantId).isEmpty()) {
                officeRepository.save(new Office(tenantId));
                System.out.println("Novo tenant '" + tenantId + "' registrado em public.offices.");
            }
            // <-- FIM DA NOVA IMPLEMENTAÇÃO -->

            // 3. Ativa o contexto do tenant para salvar o usuário no schema correto.
            TenantContext.setTenantId(tenantId);

            // 4. Cria e salva o primeiro usuário dentro do novo schema.
            User user = new User();
            user.setNome(request.getNome());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole().toString());
            user.setTenantId(tenantId);
            userRepository.save(user);

            // 5. Gera e retorna o token JWT para o novo usuário.
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
            String jwt = jwtService.generateToken(userDetails, tenantId);

            return new AuthenticationResponse(jwt);
        } catch (Exception e) {
            // Em caso de erro, a anotação @Transactional garante que nenhuma alteração será salva no banco.
            throw new RuntimeException("Erro ao criar tenant: " + e.getMessage(), e);
        } finally {
            // Limpa o contexto para a próxima requisição.
            TenantContext.clear();
        }
    }

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        // A lógica para registrar um novo usuário em um tenant já existente.
        // (Sem alterações, mas deve ser implementada se necessário)
        return null; 
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Lógica de autenticação (sem alterações)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Senha inválida");
        }
        
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        String jwt = jwtService.generateToken(userDetails, user.getTenantId());

        return new AuthenticationResponse(jwt);
    }
}