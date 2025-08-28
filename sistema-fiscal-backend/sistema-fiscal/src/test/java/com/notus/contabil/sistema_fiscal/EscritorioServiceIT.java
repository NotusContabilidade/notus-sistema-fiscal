package com.notus.contabil.sistema_fiscal;

// ✅ IMPORTS ADICIONADOS AQUI
import com.notus.contabil.sistema_fiscal.Escritorio;
import com.notus.contabil.sistema_fiscal.EscritorioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

public class EscritorioServiceIT extends AbstractIntegrationTest {

    @Autowired
    private EscritorioService escritorioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCadastrarNovoEscritorioECriarSchemaComSucesso() {
        // Arrange (Preparação)
        String razaoSocial = "Contabilidade Exemplo LTDA";
        String cnpj = "11222333000144";

        // Act (Ação)
        Escritorio escritorioSalvo = escritorioService.cadastrarNovoEscritorio(razaoSocial, cnpj);

        // Assert (Verificação)
        
        assertNotNull(escritorioSalvo);
        assertNotNull(escritorioSalvo.getId());
        assertEquals(razaoSocial, escritorioSalvo.getRazaoSocial());
        assertTrue(escritorioSalvo.getSchemaName().startsWith("escritorio_contabilidade_exemplo_ltda"));

        String sql = "SELECT COUNT(*) FROM pg_namespace WHERE nspname = ?";
        
        Integer schemaCount = jdbcTemplate.queryForObject(sql, Integer.class, escritorioSalvo.getSchemaName());

        assertNotNull(schemaCount);
        assertEquals(1, schemaCount, "O schema " + escritorioSalvo.getSchemaName() + " não foi encontrado no banco de dados.");
    }
}