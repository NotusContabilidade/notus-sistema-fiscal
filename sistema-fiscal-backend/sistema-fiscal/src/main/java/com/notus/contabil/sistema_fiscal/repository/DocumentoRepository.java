package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByClienteId(Long clienteId);
}
