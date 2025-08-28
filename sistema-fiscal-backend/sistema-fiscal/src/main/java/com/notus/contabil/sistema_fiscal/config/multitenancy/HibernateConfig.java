package com.notus.contabil.sistema_fiscal.config.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver
    ) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.notus.contabil.sistema_fiscal");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.multiTenancy", "SCHEMA");
        jpaProperties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        jpaProperties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);
        
        // ✅ ADICIONE ESTA LINHA PARA EXPOR OS METADADOS
        jpaProperties.put("hibernate.ejb.metadata", em.getPersistenceUnitInfo().getManagedClassNames());

        em.setJpaPropertyMap(jpaProperties);
        return em;
    }

    @Bean
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(DataSource dataSource) {
        return new MultiTenantConnectionProvider<String>() {
            @Override
            public Connection getAnyConnection() throws SQLException { return dataSource.getConnection(); }
            @Override
            public void releaseAnyConnection(Connection connection) throws SQLException { connection.close(); }
            @Override
            public Connection getConnection(String tenantIdentifier) throws SQLException {
                final Connection connection = getAnyConnection();
                connection.createStatement().execute(String.format("SET search_path TO %s;", tenantIdentifier));
                return connection;
            }
            @Override
            public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
                connection.createStatement().execute("SET search_path TO public;");
                releaseAnyConnection(connection);
            }
            @Override
            public boolean supportsAggressiveRelease() { return false; }
            @Override
            public boolean isUnwrappableAs(Class<?> unwrapType) { return false; }
            @Override
            public <T> T unwrap(Class<T> unwrapType) { return null; }
        };
    }

    @Bean
    public CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver<String>() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenantId = TenantContext.getCurrentTenant();
                return tenantId != null ? tenantId : "public";
            }
            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }
}