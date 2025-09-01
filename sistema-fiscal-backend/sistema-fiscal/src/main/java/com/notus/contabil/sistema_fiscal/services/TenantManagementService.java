package com.notus.contabil.sistema_fiscal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
public class TenantManagementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    public void createTenant(String tenantId) {
        // Passo 1: Cria o schema.
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\"");

        try {
            // Passo 2: Carrega o script SQL.
            Resource resource = resourceLoader.getResource("classpath:db/migration/V1__create_tenant_tables.sql");
            String sqlScript;
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                sqlScript = FileCopyUtils.copyToString(reader);
            }

            // Passo 3: MUITO IMPORTANTE - Conecta-se e define o schema para a conexão atual.
            jdbcTemplate.execute("SET search_path TO \"" + tenantId + "\"");

            // Passo 4: Executa o script de criação de tabelas DENTRO do schema correto.
            jdbcTemplate.execute(sqlScript);

            // Passo 5: Reseta o search_path para o padrão para não afetar outras operações.
            jdbcTemplate.execute("SET search_path TO public");

        } catch (Exception e) {
            throw new RuntimeException("Falha ao executar o script de criação de tabelas para o tenant: " + tenantId, e);
        }
    }
}