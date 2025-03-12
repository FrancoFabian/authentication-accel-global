package com.auth.mx.repository;

import com.auth.mx.model.UserTwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTwoFactorRepository extends JpaRepository<UserTwoFactor, Long> {
    // Para buscar por user.id o si quieres por user.email:
    Optional<UserTwoFactor> findByUserId(Long userId);
}
