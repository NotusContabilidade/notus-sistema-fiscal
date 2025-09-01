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
        try {
            // A CORREÇÃO CRUCIAL: Reseta o schema da conexão para o padrão 'public'
            // antes de devolvê-la ao pool, garantindo que ela esteja "limpa".
            connection.setSchema("public");
        } catch (SQLException e) {
            log.error("Não foi possível resetar o schema da conexão para o padrão", e);
            throw new HibernateException("Não foi possível resetar o schema da conexão para o padrão", e);
        }
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try {
            connection.setSchema(tenantIdentifier);
        } catch (SQLException e) {
            throw new HibernateException(
                "Não foi possível alterar a conexão JDBC para o schema especificado [" + tenantIdentifier + "]", e
            );
        }
        return connection;
    }
    
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        this.releaseAnyConnection(connection);
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