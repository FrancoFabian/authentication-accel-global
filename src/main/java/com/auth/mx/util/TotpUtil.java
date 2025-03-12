package com.auth.mx.util;


import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Component
public class TotpUtil {

    /**
     * Genera un secreto TOTP único.
     * Utiliza SecretGenerator.generate(int bits) con DEFAULT_BITS.
     */
    public String generateSecret() {
        byte[] secretBytes = SecretGenerator.generate(SecretGenerator.DEFAULT_BITS);
        Base32 base32 = new Base32();
        return base32.encodeAsString(secretBytes);
    }

    /**
     * Genera la URL para agregar el TOTP en aplicaciones como Google Authenticator.
     * Formato: otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     */
    public String generateTOTPUrl(String secret, String issuer, String account) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, account, secret, issuer);
    }
    /**
     * Verifica si el código ingresado es válido para el secreto dado.
     * Se espera que el secreto esté en formato Base32.
     */
    public boolean verifyCode(String secret, String code) {
        Base32 base32 = new Base32();
        // Decodificar el secreto en Base32 a byte[]
        byte[] secretBytes = base32.decode(secret);
        // Crear el generador TOTP usando el secret
        TOTPGenerator generator = TOTPGenerator.withDefaultValues(secretBytes);
        // Generar el código esperado para el instante actual
        String expectedCode = generator.now();
        return expectedCode.equals(code);
    }
}