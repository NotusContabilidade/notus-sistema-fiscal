package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCnpj(String cnpj);

    // ✅ ALTERAÇÃO: Removido "public." e "simples_nacional."
    @Query(value = "SELECT COUNT(c.id) FROM clientes c WHERE NOT EXISTS " +
                   "(SELECT 1 FROM calculos ca WHERE ca.cliente_id = c.id AND ca.ano_referencia = :ano AND ca.mes_referencia = :mes)",
           nativeQuery = true)
    long countClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);

    // ✅ ALTERAÇÃO: Removido "public." e "simples_nacional."
    @Query(value = "SELECT c.id, c.razao_social FROM clientes c WHERE NOT EXISTS " +
                   "(SELECT 1 FROM calculos ca WHERE ca.cliente_id = c.id AND ca.ano_referencia = :ano AND ca.mes_referencia = :mes)",
           nativeQuery = true)
    List<Object[]> findClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);
}