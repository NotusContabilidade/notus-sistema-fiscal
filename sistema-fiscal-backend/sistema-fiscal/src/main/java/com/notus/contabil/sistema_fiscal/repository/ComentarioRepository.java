package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTaskIdOrderByDataCriacaoAsc(Long taskId);
}