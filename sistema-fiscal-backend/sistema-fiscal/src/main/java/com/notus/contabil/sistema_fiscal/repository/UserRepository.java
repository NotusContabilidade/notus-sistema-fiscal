package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Método essencial para o Spring Security encontrar o usuário pelo email (que estamos usando como username)
    Optional<User> findByEmail(String email);

}