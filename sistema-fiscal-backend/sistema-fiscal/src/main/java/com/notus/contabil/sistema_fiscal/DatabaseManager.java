package com.notus.contabil.sistema_fiscal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static final HikariDataSource ds;

    static {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Arquivo 'application.properties' não encontrado no classpath.");
            }
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

    private static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public record Cliente(Long id, String cnpj, String razaoSocial) {}
    public record ParametrosSN(Long id, Long clienteId, double rbt12Atual, double folhaPagamento12mAtual) {}
    public record Calculo(
        long id, long clienteId, int mesReferencia, int anoReferencia, double rbt12, 
        double aliquotaEfetiva, Double fatorR, String anexoAplicado, double rpaTotal, 
        double dasTotal, double rpaNormal, double dasNormal, double rpaComRetencao, 
        double dasComRetencaoLiquido, double issRetido, double rpaStICMS, double dasStICMS
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
        
        // --- MÉTODO SALVAR ATUALIZADO ---
        public long salvar(long clienteId, int mesRef, int anoRef, ResultadoCalculoDetalhado r) {
            // 1. Tenta encontrar um cálculo existente para este período
            Optional<Long> existingIdOpt = findIdByClienteAndPeriodo(clienteId, mesRef, anoRef);

            if (existingIdOpt.isPresent()) {
                // 2. Se existe, ATUALIZA o registro
                long existingId = existingIdOpt.get();
                update(existingId, r);
                return existingId;
            } else {
                // 3. Se não existe, INSERE um novo registro
                return insert(clienteId, mesRef, anoRef, r);
            }
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

        private long insert(long clienteId, int mesRef, int anoRef, ResultadoCalculoDetalhado r) {
            String sql = """
                INSERT INTO simples_nacional.calculos 
                (cliente_id, mes_referencia, ano_referencia, rbt12, aliquota_efetiva, fator_r, anexo_aplicado, rpa_total, das_total, rpa_normal, das_normal, rpa_com_retencao, das_com_retencao_liquido, iss_retido, rpa_st_icms, das_st_icms) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(pstmt, clienteId, mesRef, anoRef, r);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao inserir o cálculo: " + e.getMessage(), e);
            }
            throw new IllegalStateException("Não foi possível inserir o cálculo e obter o ID.");
        }

        private void update(long calculoId, ResultadoCalculoDetalhado r) {
            String sql = """
                UPDATE simples_nacional.calculos SET 
                rbt12 = ?, aliquota_efetiva = ?, fator_r = ?, anexo_aplicado = ?, rpa_total = ?, das_total = ?, rpa_normal = ?, das_normal = ?, rpa_com_retencao = ?, das_com_retencao_liquido = ?, iss_retido = ?, rpa_st_icms = ?, das_st_icms = ?, data_calculo = CURRENT_TIMESTAMP
                WHERE id = ?
            """;
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Note que os parâmetros aqui estão em uma ordem diferente do INSERT
                pstmt.setDouble(1, r.rbt12());
                pstmt.setDouble(2, r.aliquotaEfetivaTotal());
                if (r.fatorR() != null) pstmt.setDouble(3, r.fatorR()); else pstmt.setNull(3, Types.DOUBLE);
                pstmt.setString(4, r.anexoAplicado());
                pstmt.setDouble(5, r.rpaTotal());
                pstmt.setDouble(6, r.dasTotal());
                pstmt.setDouble(7, r.rpaNormal());
                pstmt.setDouble(8, r.dasNormal());
                pstmt.setDouble(9, r.rpaComRetencao());
                pstmt.setDouble(10, r.dasComRetencaoLiquido());
                pstmt.setDouble(11, r.issRetido());
                pstmt.setDouble(12, r.rpaStICMS());
                pstmt.setDouble(13, r.dasStICMS());
                pstmt.setLong(14, calculoId); // O último parâmetro é o ID
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao atualizar o cálculo: " + e.getMessage(), e);
            }
        }

        // Método auxiliar para evitar repetição de código
        private void setParameters(PreparedStatement pstmt, long clienteId, int mesRef, int anoRef, ResultadoCalculoDetalhado r) throws SQLException {
            pstmt.setLong(1, clienteId);
            pstmt.setInt(2, mesRef);
            pstmt.setInt(3, anoRef);
            pstmt.setDouble(4, r.rbt12());
            pstmt.setDouble(5, r.aliquotaEfetivaTotal());
            if (r.fatorR() != null) pstmt.setDouble(6, r.fatorR()); else pstmt.setNull(6, Types.DOUBLE);
            pstmt.setString(7, r.anexoAplicado());
            pstmt.setDouble(8, r.rpaTotal());
            pstmt.setDouble(9, r.dasTotal());
            pstmt.setDouble(10, r.rpaNormal());
            pstmt.setDouble(11, r.dasNormal());
            pstmt.setDouble(12, r.rpaComRetencao());
            pstmt.setDouble(13, r.dasComRetencaoLiquido());
            pstmt.setDouble(14, r.issRetido());
            pstmt.setDouble(15, r.rpaStICMS());
            pstmt.setDouble(16, r.dasStICMS());
        }

        public Optional<Calculo> findById(long calculoId) {
            // ... (código do findById permanece o mesmo)
            String sql = "SELECT * FROM simples_nacional.calculos WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, calculoId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double fatorRValue = rs.getDouble("fator_r");
                        Double fatorR = rs.wasNull() ? null : fatorRValue;

                        return Optional.of(new Calculo(
                            rs.getLong("id"), rs.getLong("cliente_id"), rs.getInt("mes_referencia"),
                            rs.getInt("ano_referencia"), rs.getDouble("rbt12"), rs.getDouble("aliquota_efetiva"),
                            fatorR, rs.getString("anexo_aplicado"), rs.getDouble("rpa_total"),
                            rs.getDouble("das_total"), rs.getDouble("rpa_normal"), rs.getDouble("das_normal"),
                            rs.getDouble("rpa_com_retencao"), rs.getDouble("das_com_retencao_liquido"),
                            rs.getDouble("iss_retido"), rs.getDouble("rpa_st_icms"), rs.getDouble("das_st_icms")
                        ));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar cálculo por ID: " + e.getMessage(), e);
            }
            return Optional.empty();
        }
    }
}
