package com.auth.mx.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class EncryptionUtil {
    private static final String AES_ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 256;

    public static String encryptWithRSA(String data, RSAPublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decryptWithRSA(String encryptedData, RSAPrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    public static String encryptWithAES(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decryptWithAES(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public static String encryptAESKeyWithRSA(SecretKey aesKey, RSAPublicKey publicKey) throws Exception {
        return encryptWithRSA(Base64.getEncoder().encodeToString(aesKey.getEncoded()), publicKey);
    }

    public static SecretKey decryptAESKeyWithRSA(String encryptedKey, RSAPrivateKey privateKey) throws Exception {
        String decryptedKeyStr = decryptWithRSA(encryptedKey, privateKey);
        byte[] keyBytes = Base64.getDecoder().decode(decryptedKeyStr);
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
}
