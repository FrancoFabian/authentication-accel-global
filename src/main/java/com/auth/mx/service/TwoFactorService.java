package com.auth.mx.service;

import com.auth.mx.dto.AuthResponse;
import com.auth.mx.dto.Enable2FAResponse;
import com.auth.mx.model.Role;
import com.auth.mx.model.User;
import com.auth.mx.model.UserTwoFactor;
import com.auth.mx.repository.UserRepository;
import com.auth.mx.repository.UserTwoFactorRepository;
import com.auth.mx.security.JwtService;
import com.auth.mx.util.TotpUtil;
import com.bastiaanjansen.otp.TOTPGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserTwoFactorRepository userTwoFactorRepository;
    private final UserRepository userRepository;
    private final TotpUtil totpUtil;
    private final JwtService jwtService;
    private final WhatsAppService whatsAppService;

    public Enable2FAResponse enable2FA(Long userId, String method) {
        // Buscar el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar o crear la configuración 2FA para el usuario
        UserTwoFactor config = userTwoFactorRepository.findByUserId(user.getId())
                .orElse(UserTwoFactor.builder().user(user).build());

        // Generar el secreto TOTP (en Base32) usando TotpUtil
        String secret = totpUtil.generateSecret();
        config.setTwoFactorSecret(secret);
        config.setTwoFactorEnabled(true);
        config.setTwoFactorMethod(method);

        // Guardar la configuración
        userTwoFactorRepository.save(config);

        // Generar la URL de tipo "otpauth://..."
        String totpUrl = totpUtil.generateTOTPUrl(secret, "TuApp", user.getEmail());

        // Si se selecciona el método WHATSAPP, enviar el código actual al número del usuario
        if ("WHATSAPP".equalsIgnoreCase(method)) {
            // Replicar la generación del código TOTP
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(secret);
            TOTPGenerator generator = TOTPGenerator.withDefaultValues(secretBytes);
            String currentCode = generator.now();
            whatsAppService.sendWhatsAppMessage(user.getNumberPhone(),
                    "Tu código de autenticación es: " + currentCode);
        }

        return new Enable2FAResponse(totpUrl, secret);
    }
    public AuthResponse verify2FA(String email, String code) {
        // 1) Buscar usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Validar si 2FA está habilitado
        if (!user.isTwoFactorEnabled()) {
            throw new RuntimeException("El usuario no tiene 2FA habilitado");
        }

        // 3) Según el metodo. Si es TOTP, usamos totpUtil; si es SMS/EMAIL, sería otro flujo
        String method = user.getTwoFactorMethod();  // TOTP, SMS, etc.
        if ("TOTP".equalsIgnoreCase(method)) {
            // Verificamos con la librería TOTP
            boolean valid = totpUtil.verifyCode(user.getTwoFactorSecret(), code);
            if (!valid) {
                throw new RuntimeException("Código TOTP inválido");
            }
        } else if ("SMS".equalsIgnoreCase(method)) {
            // Ejemplo: verificar un code guardado temporalmente en la BD
            // ...
            throw new UnsupportedOperationException("Verificación por SMS no implementada");
        } else {
            // ...
            throw new RuntimeException("Método 2FA desconocido: " + method);
        }

        // 4) Si es válido, generamos el JWT
        String token = jwtService.generateToken(user);

        // 5) Construimos la respuesta (usando tu AuthResponse)
        var rolesEnTexto = user.getRoles().stream()
                .map(Role::getNombre)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .email(user.getEmail())
                .numberPhone(user.getNumberPhone())
                .token(token)
                .role(rolesEnTexto)
                .twoFactorRequired(false)
                .build();
    }
}
