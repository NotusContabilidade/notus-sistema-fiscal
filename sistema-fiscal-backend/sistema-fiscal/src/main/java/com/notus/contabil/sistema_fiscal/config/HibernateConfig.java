package com.notus.contabil.sistema_fiscal.config;

import com.notus.contabil.sistema_fiscal.config.multitenancy.MultiTenantConnectionProviderImpl;
import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaProperties jpaProperties,
            MultiTenantConnectionProviderImpl multiTenantConnectionProvider,
            TenantIdentifierResolver tenantIdentifierResolver) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.notus.contabil.sistema_fiscal");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.multi_tenancy", "SCHEMA"); // ✅ USANDO TEXTO DIRETO
        properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
        properties.putAll(jpaProperties.getProperties());

        emf.setJpaPropertyMap(properties);
        return emf;
    }
}