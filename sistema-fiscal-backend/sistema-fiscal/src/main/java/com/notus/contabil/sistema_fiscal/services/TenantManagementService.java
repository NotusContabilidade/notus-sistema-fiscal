package com.notus.contabil.sistema_fiscal.services;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TenantManagementService {

    private final DataSource dataSource;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String flywayLocations;

    public TenantManagementService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTenant(String schemaName) throws Exception {
        schemaName = schemaName.toLowerCase();
        // Cria o schema do tenant se não existir
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        }

        // Aplica as migrations no schema do tenant
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations(flywayLocations)
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }
}