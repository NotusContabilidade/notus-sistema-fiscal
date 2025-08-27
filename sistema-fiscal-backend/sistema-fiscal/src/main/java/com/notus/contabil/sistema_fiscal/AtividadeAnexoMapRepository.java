package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AtividadeAnexoMapRepository extends JpaRepository<AtividadeAnexoMap, Long> {

    Optional<AtividadeAnexoMap> findByCodigoServico(String codigoServico);

}