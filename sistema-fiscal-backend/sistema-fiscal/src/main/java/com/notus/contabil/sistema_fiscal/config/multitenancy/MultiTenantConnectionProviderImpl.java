package com.notus.contabil.sistema_fiscal.config.multitenancy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<Object> {

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
        // Apenas devolve a conexão ao pool.
        connection.close();
    }

    /**
     * Obtém uma conexão e a configura para o tenantId especificado.
     * Esta versão é segura contra SQL Injection.
     */
    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        String tenantId = (String) tenantIdentifier;
        log.debug("Obtendo conexão para o tenant: {}", tenantId);

        final Connection connection = getAnyConnection();

        try {
            // 1. Sanitize o tenantId para previnir SQL Injection
            String safeTenantId;
            try (PreparedStatement ps = connection.prepareStatement("SELECT quote_ident(?)")) {
                ps.setString(1, tenantId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    safeTenantId = rs.getString(1);
                } else {
                    throw new HibernateException("Não foi possível sanitizar o tenant ID: " + tenantId);
                }
            }

            // 2. Define o search_path com o identificador seguro
            try (var statement = connection.createStatement()) {
                statement.execute("SET search_path TO " + safeTenantId + ", public");
                log.debug("search_path definido para: {}", safeTenantId);
            }

        } catch (SQLException e) {
            log.error("Não foi possível alterar o search_path para o tenant '{}'", tenantId, e);
            // Libera a conexão em caso de falha
            releaseAnyConnection(connection);
            throw new HibernateException("Não foi possível definir search_path para o tenant: " + tenantId, e);
        }

        return connection;
    }

    /**
     * Libera a conexão, resetando o search_path para o padrão.
     */
    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        log.debug("Liberando conexão para o tenant: {}", tenantIdentifier);
        try {
            // Reseta o schema para o padrão antes de devolver a conexão ao pool
            try (var statement = connection.createStatement()) {
                statement.execute("SET search_path TO public");
            }
        } catch (SQLException e) {
            // Não relance a exceção aqui, pois pode mascarar a exceção original
            // Apenas logue o erro. A conexão ainda precisa ser fechada.
            log.error("Não foi possível resetar o search_path para 'public' no tenant '{}'", tenantIdentifier, e);
        } finally {
            // Devolve a conexão para o pool
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    // Métodos isUnwrappableAs e unwrap não são necessários com a implementação direta
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}