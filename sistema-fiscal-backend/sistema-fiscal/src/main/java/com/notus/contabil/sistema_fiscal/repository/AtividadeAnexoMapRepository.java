package com.notus.contabil.sistema_fiscal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notus.contabil.sistema_fiscal.services.AtividadeAnexoMap;

@Repository
public interface AtividadeAnexoMapRepository extends JpaRepository<AtividadeAnexoMap, Long> {

    Optional<AtividadeAnexoMap> findByCodigoServico(String codigoServico);

}