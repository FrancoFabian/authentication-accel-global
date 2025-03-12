package com.auth.mx.repository;

import com.auth.mx.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Método extra para buscar por nombre de permiso
    Optional<Permission> findByNombre(String nombre);
}
