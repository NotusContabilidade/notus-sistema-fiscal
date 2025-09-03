package com.notus.contabil.sistema_fiscal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Service
public class TenantManagementService {

    private static final Logger log = LoggerFactory.getLogger(TenantManagementService.class);
    private final JdbcTemplate jdbcTemplate;

    @Value("classpath:db/migration/V1__init_tenant_schema.sql")
    private Resource schemaSql;

    public TenantManagementService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createTenant(String tenantId) {
        if (!tenantId.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Nome de tenant inválido.");
        }

        log.info("Criando schema do tenant '{}'", tenantId);
        try {
            // Cria o schema
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\";");

            // Cria a tabela users no schema do tenant
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"" + tenantId + "\".users (" +
                "id SERIAL PRIMARY KEY, " +
                "nome VARCHAR(255), " +
                "email VARCHAR(255), " +
                "password VARCHAR(255), " +
                "role VARCHAR(50), " +
                "tenantId VARCHAR(50)" +
                ");"
            );

            // Se quiser rodar o script SQL completo, descomente abaixo:
             String sqlScript = StreamUtils.copyToString(schemaSql.getInputStream(), StandardCharsets.UTF_8);
             jdbcTemplate.execute("SET search_path TO " + tenantId + ", \"$user\", public");
             jdbcTemplate.execute(sqlScript);

        } catch (Exception e) {
            log.error("Erro ao criar tenant '{}'", tenantId, e);
            throw new RuntimeException("Falha ao criar tenant: " + tenantId, e);
        } finally {
            resetSearchPath();
        }
    }

    public void resetSearchPath() {
        try {
            jdbcTemplate.execute("SET search_path TO \"$user\", public");
        } catch (Exception e) {
            log.error("Erro ao resetar search_path", e);
        }
    }
}