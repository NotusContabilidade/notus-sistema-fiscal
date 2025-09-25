package com.notus.contabil.sistema_fiscal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.notus.contabil.sistema_fiscal.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Este método gerado automaticamente pelo Spring Data JPA já é seguro e ciente do tenant.
    Optional<Cliente> findByCnpj(String cnpj);

    // ✅ QUERY REATORADA PARA JPQL - AGORA É SEGURA
    // O Hibernate vai adicionar a cláusula "WHERE tenant_id = ?" automaticamente nesta consulta.
    @Query("SELECT count(c.id) FROM Cliente c WHERE NOT EXISTS " +
           "(SELECT 1 FROM Calculo ca WHERE ca.cliente.id = c.id AND ca.anoReferencia = :ano AND ca.mesReferencia = :mes)")
    long countClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);

    // ✅ QUERY REATORADA PARA JPQL - SEGURA E MAIS LIMPA
    // O Hibernate também adiciona o filtro de tenant aqui.
    // O retorno de List<Cliente> é mais orientado a objetos do que List<Object[]>.
    @Query("SELECT c FROM Cliente c WHERE NOT EXISTS " +
           "(SELECT 1 FROM Calculo ca WHERE ca.cliente.id = c.id AND ca.anoReferencia = :ano AND ca.mesReferencia = :mes)")
    List<Cliente> findClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);

    // --- MÉTODOS PARA A BUSCA INTELIGENTE ---
    List<Cliente> findByRazaoSocialContainingIgnoreCase(String razaoSocial);

    @Query("SELECT c FROM Cliente c WHERE c.cnpj LIKE %:cnpj%")
    List<Cliente> findByCnpjContaining(@Param("cnpj") String cnpj);
    // --- FIM DOS MÉTODOS DE BUSCA ---

    // Busca por e-mail (para autenticação do cliente)
    Optional<Cliente> findByEmail(String email);
}