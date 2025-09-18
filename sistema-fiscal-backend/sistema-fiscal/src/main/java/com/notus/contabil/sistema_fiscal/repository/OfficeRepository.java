package com.notus.contabil.sistema_fiscal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notus.contabil.sistema_fiscal.entity.Office; // Importe Optional

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {
    // MÉTODO ADICIONADO para buscar um tenant pelo nome
    Optional<Office> findByName(String name);
}