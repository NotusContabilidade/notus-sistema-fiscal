package com.notus.contabil.sistema_fiscal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.services.TenantManagementService;

@SpringBootTest
@Testcontainers
class ClienteRepositoryTest {

    @Autowired
    private TenantManagementService tenantManagementService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        // Nenhum ddl-auto aqui, o TenantManagementService tem controle total.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("findAll deve retornar apenas clientes do schema do tenant atual")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deve_isolar_clientes_por_tenant() {
        String tenantA = "escritorio_a";
        String tenantB = "escritorio_b";
        
        tenantManagementService.createTenant(tenantA);
        tenantManagementService.createTenant(tenantB);

        // Transação 1: Salva cliente A
        TenantContext.setTenantId(tenantA);
        Cliente clienteA = new Cliente();
        clienteA.setRazaoSocial("Cliente A LTDA");
        clienteA.setCnpj("11111111000111");
        clienteRepository.saveAndFlush(clienteA);

        // Transação 2: Salva cliente B
        TenantContext.setTenantId(tenantB);
        Cliente clienteB = new Cliente();
        clienteB.setRazaoSocial("Cliente B SA");
        clienteB.setCnpj("22222222000122");
        clienteRepository.saveAndFlush(clienteB);

        // Transação 3: Busca no tenant A
        TenantContext.setTenantId(tenantA);
        List<Cliente> clientesEncontrados = clienteRepository.findAll();

        // A verificação correta, esperando 1
        assertThat(clientesEncontrados).hasSize(1);
        assertThat(clientesEncontrados.get(0).getCnpj()).isEqualTo("11111111000111");
    }
}