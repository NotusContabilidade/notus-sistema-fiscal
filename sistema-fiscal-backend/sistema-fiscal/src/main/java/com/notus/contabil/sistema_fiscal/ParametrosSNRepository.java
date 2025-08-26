package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ParametrosSNRepository extends JpaRepository<ParametrosSN, Long> {
    Optional<ParametrosSN> findByClienteId(Long clienteId);
}