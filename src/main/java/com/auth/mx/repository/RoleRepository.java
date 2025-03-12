package com.auth.mx.repository;

import com.auth.mx.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Método extra para buscar por nombre de rol
    Optional<Role> findByNombre(String nombre);
}
