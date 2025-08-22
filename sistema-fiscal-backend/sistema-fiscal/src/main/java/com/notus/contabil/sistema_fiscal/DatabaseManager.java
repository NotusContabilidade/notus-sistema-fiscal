package com.notus.contabil.sistema_fiscal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {
    private static final HikariDataSource ds;
    static {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) throw new IllegalStateException("Arquivo 'application.properties' não encontrado.");
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao carregar o arquivo de propriedades.", ex);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("spring.datasource.url"));
        config.setUsername(properties.getProperty("spring.datasource.username"));
        config.setPassword(properties.getProperty("spring.datasource.password"));
        ds = new HikariDataSource(config);
    }
    public final ClienteDAO clienteDAO = new ClienteDAO();
    public final ParametrosSNDAO parametrosSNDAO = new ParametrosSNDAO();
    public final CalculoDAO calculoDAO = new CalculoDAO();
    private static Connection getConnection() throws SQLException { return ds.getConnection(); }

    public record Cliente(Long id, String cnpj, String razaoSocial) {}
    public record ParametrosSN(Long id, Long clienteId, double rbt12Atual, double folhaPagamento12mAtual) {}
    
    // --- RECORD ATUALIZADO ---
    public record Calculo(
        long id, long clienteId, int mesReferencia, int anoReferencia, 
        double dasTotal,
        String dataCalculo, // Adicionado o campo para a data
        List<ResultadoCalculoDetalhado> detalhes
    ) {}

    public class ClienteDAO { 
        // ... (código do ClienteDAO permanece o mesmo)
        public Cliente save(String cnpj, String razaoSocial) {
            String sql = "INSERT INTO public.clientes (cnpj, razao_social) VALUES (?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, cnpj);
                pstmt.setString(2, razaoSocial);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        return new Cliente(id, cnpj, razaoSocial);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao salvar cliente: " + e.getMessage(), e);
            }
            throw new IllegalStateException("Não foi possível salvar o cliente e obter o ID.");
        }

        public Optional<Cliente> findByCnpj(String cnpj) {
            String sql = "SELECT id, razao_social FROM public.clientes WHERE cnpj = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cnpj);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Cliente(rs.getLong("id"), cnpj, rs.getString("razao_social")));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar cliente por CNPJ: " + e.getMessage(), e);
            }
            return Optional.empty();
        }

        public Optional<Cliente> findById(Long id) {
            String sql = "SELECT cnpj, razao_social FROM public.clientes WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Cliente(id, rs.getString("cnpj"), rs.getString("razao_social")));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar cliente por ID: " + e.getMessage(), e);
            }
            return Optional.empty();
        }
    }
    public class ParametrosSNDAO { 
        // ... (código do ParametrosSNDAO permanece o mesmo)
        public void save(long clienteId, double rbt12, double folha12m) {
            String sql = "INSERT INTO simples_nacional.parametros_sn (cliente_id, rbt12_atual, folha_pagamento_12m_atual) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, clienteId);
                pstmt.setDouble(2, rbt12);
                pstmt.setDouble(3, folha12m);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao salvar parâmetros: " + e.getMessage(), e);
            }
        }

        public Optional<ParametrosSN> findByClienteId(Long clienteId) {
            String sql = "SELECT id, rbt12_atual, folha_pagamento_12m_atual FROM simples_nacional.parametros_sn WHERE cliente_id = ? ORDER BY id DESC LIMIT 1";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, clienteId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new ParametrosSN(
                            rs.getLong("id"),
                            clienteId,
                            rs.getDouble("rbt12_atual"),
                            rs.getDouble("folha_pagamento_12m_atual")
                        ));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar parâmetros: " + e.getMessage(), e);
            }
            return Optional.empty();
        }
    }

    public class CalculoDAO {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        public long salvar(long clienteId, int mesRef, int anoRef, CalculadoraSimplesNacional.ResultadoGeralCalculo resultadoGeral) {
            String detalhesJson;
            try {
                detalhesJson = objectMapper.writeValueAsString(resultadoGeral.detalhes());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro ao serializar detalhes do cálculo para JSON", e);
            }

            Optional<Long> existingIdOpt = findIdByClienteAndPeriodo(clienteId, mesRef, anoRef);
            if (existingIdOpt.isPresent()) {
                update(existingIdOpt.get(), resultadoGeral.dasTotalGeral(), detalhesJson);
                return existingIdOpt.get();
            } else {
                return insert(clienteId, mesRef, anoRef, resultadoGeral.dasTotalGeral(), detalhesJson);
            }
        }
        
        public List<Calculo> findAllByClienteId(Long clienteId) {
            List<Calculo> historico = new ArrayList<>();
            String sql = "SELECT * FROM simples_nacional.calculos WHERE cliente_id = ? ORDER BY ano_referencia DESC, mes_referencia DESC";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, clienteId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        historico.add(mapRowToCalculo(rs));
                    }
                }
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException("Erro ao buscar histórico de cálculos", e);
            }
            return historico;
        }

        private void update(long calculoId, double dasTotalGeral, String detalhesJson) {
            String sql = "UPDATE simples_nacional.calculos SET das_total = ?, detalhes_json = ?, data_calculo = CURRENT_TIMESTAMP WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, dasTotalGeral);
                pstmt.setString(2, detalhesJson);
                pstmt.setLong(3, calculoId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao atualizar o cálculo", e);
            }
        }

        private long insert(long clienteId, int mesRef, int anoRef, double dasTotalGeral, String detalhesJson) {
            String sql = "INSERT INTO simples_nacional.calculos (cliente_id, mes_referencia, ano_referencia, das_total, detalhes_json) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, clienteId);
                pstmt.setInt(2, mesRef);
                pstmt.setInt(3, anoRef);
                pstmt.setDouble(4, dasTotalGeral);
                pstmt.setString(5, detalhesJson);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao inserir o cálculo", e);
            }
            throw new IllegalStateException("Não foi possível inserir o cálculo.");
        }

        public Optional<Calculo> findById(long calculoId) {
            String sql = "SELECT * FROM simples_nacional.calculos WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, calculoId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToCalculo(rs));
                    }
                }
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException("Erro ao buscar ou deserializar cálculo por ID", e);
            }
            return Optional.empty();
        }

        private Optional<Long> findIdByClienteAndPeriodo(long clienteId, int mesRef, int anoRef) {
            String sql = "SELECT id FROM simples_nacional.calculos WHERE cliente_id = ? AND mes_referencia = ? AND ano_referencia = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, clienteId);
                pstmt.setInt(2, mesRef);
                pstmt.setInt(3, anoRef);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("id"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar cálculo existente: " + e.getMessage(), e);
            }
            return Optional.empty();
        }

        // --- NOVO MÉTODO AUXILIAR PARA EVITAR REPETIÇÃO ---
        private Calculo mapRowToCalculo(ResultSet rs) throws SQLException, JsonProcessingException {
            String detalhesJson = rs.getString("detalhes_json");
            List<ResultadoCalculoDetalhado> detalhes = (detalhesJson == null || detalhesJson.isBlank())
                ? List.of()
                : objectMapper.readValue(detalhesJson, new TypeReference<>() {});
            
            Timestamp dataCalculoTs = rs.getTimestamp("data_calculo");
            String dataCalculoStr = (dataCalculoTs != null) ? dateFormat.format(dataCalculoTs) : "N/D";
            
            return new Calculo(
                rs.getLong("id"),
                rs.getLong("cliente_id"),
                rs.getInt("mes_referencia"),
                rs.getInt("ano_referencia"),
                rs.getDouble("das_total"),
                dataCalculoStr,
                detalhes
            );
        }
    }
}