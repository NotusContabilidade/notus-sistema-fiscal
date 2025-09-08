package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
