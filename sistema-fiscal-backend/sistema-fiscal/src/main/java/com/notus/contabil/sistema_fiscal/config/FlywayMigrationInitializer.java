package com.notus.contabil.sistema_fiscal.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.notus.contabil.sistema_fiscal.repository.OfficeRepository;

@Component
public class FlywayMigrationInitializer {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private OfficeRepository officeRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateTenantSchemas() {
        System.out.println(">>> Aplicação pronta. Iniciando migrações do Flyway para todos os tenants...");

        runFlywayForSchema("public");

        officeRepository.findAll().forEach(office -> {
            String tenantId = office.getName().toLowerCase();
            runFlywayForSchema(tenantId);
        });
        
        System.out.println(">>> Todas as migrações do Flyway foram concluídas.");
    }

    private void runFlywayForSchema(String schema) {
        System.out.println("Executando Flyway para o schema: " + schema);
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .outOfOrder(true) // <-- CORREÇÃO ADICIONADA AQUI
                    .load();

            flyway.repair();
            flyway.migrate();
            System.out.println("Flyway para o schema '" + schema + "' concluído com sucesso.");
        } catch (Exception e) {
            System.err.println("ERRO ao executar Flyway para o schema '" + schema + "': " + e.getMessage());
        }
    }
}