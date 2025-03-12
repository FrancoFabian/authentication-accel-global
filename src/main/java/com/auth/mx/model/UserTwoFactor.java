package com.auth.mx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla separada donde guardamos la configuración 2FA de cada User.
 *
 * Ejemplo:
 *  - twoFactorEnabled
 *  - twoFactorSecret (clave TOTP)
 *  - tipoDe2FA (OPCIONAL: TOTP, SMS, EMAIL, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_two_factor")
public class UserTwoFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1:1 con User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "two_factor_method")
    private String twoFactorMethod;  // "TOTP", "SMS", "EMAIL", "WHATSAPP", etc.
}
