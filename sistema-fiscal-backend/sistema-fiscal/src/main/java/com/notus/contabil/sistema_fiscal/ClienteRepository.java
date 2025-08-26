package com.notus.contabil.sistema_fiscal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCnpj(String cnpj);

    @Query(value = "SELECT COUNT(c.id) FROM public.clientes c WHERE NOT EXISTS " +
           "(SELECT 1 FROM simples_nacional.calculos ca WHERE ca.cliente_id = c.id AND ca.ano_referencia = :ano AND ca.mes_referencia = :mes)",
           nativeQuery = true)
    long countClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);

    // ✅ CORREÇÃO FINAL APLICADA AQUI: O tipo de retorno agora é List<Object[]>
    @Query(value = "SELECT c.id, c.razao_social FROM public.clientes c WHERE NOT EXISTS " +
                   "(SELECT 1 FROM simples_nacional.calculos ca WHERE ca.cliente_id = c.id AND ca.ano_referencia = :ano AND ca.mes_referencia = :mes)",
           nativeQuery = true)
    List<Object[]> findClientesSemCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);
}