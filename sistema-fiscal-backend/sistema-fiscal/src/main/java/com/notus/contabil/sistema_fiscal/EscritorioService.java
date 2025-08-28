package com.notus.contabil.sistema_fiscal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.tool.schema.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class EscritorioService {

    @Autowired
    private EscritorioRepository escritorioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LocalContainerEntityManagerFactoryBean emf; // Injetamos a fábrica do JPA

    @Transactional
    public Escritorio cadastrarNovoEscritorio(String razaoSocial, String cnpj) {
        String schemaName = "escritorio_" + razaoSocial.toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^a-z0-9_]", "") + "_" + System.currentTimeMillis() % 10000;

        // 1. Salva o escritório no schema 'public'
        Escritorio novoEscritorio = new Escritorio();
        novoEscritorio.setRazaoSocial(razaoSocial);
        novoEscritorio.setCnpj(cnpj.replaceAll("[^0-9]", ""));
        novoEscritorio.setSchemaName(schemaName);
        escritorioRepository.save(novoEscritorio);

        // 2. Cria o schema no banco de dados
        String createSchemaSql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        entityManager.createNativeQuery(createSchemaSql).executeUpdate();

        // 3. USA O HIBERNATE PARA CRIAR AS TABELAS NO NOVO SCHEMA
        // Pega os metadados de todas as entidades (@Entity) do projeto
        var metadata = emf.getJpaPropertyMap().get("hibernate.ejb.metadata");
        
        // Configura a ação de exportação do schema (DDL)
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true); // Formata o SQL
        
        // Define que a ação será EXECUTADA no banco de dados
        EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.DATABASE);
        
        // Executa a criação das tabelas no schema recém-criado
        entityManager.unwrap(Session.class).doWork(connection -> {
            connection.createStatement().execute("SET search_path TO " + schemaName);
            schemaExport.execute(targetTypes, SchemaExport.Action.CREATE, null, metadata);
        });

        return novoEscritorio;
    }
}