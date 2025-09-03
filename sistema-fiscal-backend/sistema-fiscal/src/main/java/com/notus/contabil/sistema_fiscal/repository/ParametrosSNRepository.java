package com.notus.contabil.sistema_fiscal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notus.contabil.sistema_fiscal.entity.ParametrosSN;

@Repository
public interface ParametrosSNRepository extends JpaRepository<ParametrosSN, Long> {
    Optional<ParametrosSN> findByClienteId(Long clienteId);
}