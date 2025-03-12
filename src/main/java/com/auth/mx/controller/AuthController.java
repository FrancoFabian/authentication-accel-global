package com.auth.mx.controller;

import com.auth.mx.dto.AuthRequest;
import com.auth.mx.dto.AuthResponse;
import com.auth.mx.dto.TwoFactorRequest;
import com.auth.mx.model.User;
import com.auth.mx.repository.UserRepository;
import com.auth.mx.service.AuthService;
import com.auth.mx.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    /**
     * Permite habilitar la doble autenticación para un usuario dado.
     * EJEMPLO de petición:
     * POST /api/auth/enable-2fa?userId=123&method=TOTP
     */
    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2FA(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "TOTP") String method
    ) {
        // Llamamos al servicio
        var response = twoFactorService.enable2FA(userId, method);
        // Este response es de tipo Enable2FAResponse (totpUrl, secret)
        return ResponseEntity.ok(response);
    }
    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponse> verify2FA(@RequestBody TwoFactorRequest request) {
        // El servicio se encargará de:
        // 1) Buscar al usuario
        // 2) Validar que esté habilitado 2FA
        // 3) Comparar el código con el TOTP o método que toque
        // 4) Si es válido, generar el JWT
        // 5) Retornar AuthResponse con token
        AuthResponse response = twoFactorService.verify2FA(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-2fa")
    public ResponseEntity<Boolean> checkTwoFactor(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(user.isTwoFactorEnabled());
    }


}
