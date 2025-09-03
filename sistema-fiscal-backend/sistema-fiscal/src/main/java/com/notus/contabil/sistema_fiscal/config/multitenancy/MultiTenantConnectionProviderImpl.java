package com.notus.contabil.sistema_fiscal.config.multitenancy;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private static final Logger log = LoggerFactory.getLogger(MultiTenantConnectionProviderImpl.class);
    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.info(">>> Conexão solicitada para o tenant: {}", tenantIdentifier);
        final Connection connection = getAnyConnection();
        try {
            connection.setSchema(tenantIdentifier);
            log.info(">>> Schema definido para '{}'", tenantIdentifier);
        } catch (SQLException e) {
            log.error("!!! Erro ao definir schema '{}'", tenantIdentifier, e);
            throw new HibernateException("Não foi possível definir schema para: " + tenantIdentifier, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            connection.setSchema("public");
            log.info(">>> Schema resetado para 'public'");
        } catch (SQLException e) {
            log.error("!!! Erro ao resetar schema para 'public'", e);
            throw new HibernateException("Não foi possível resetar schema", e);
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
