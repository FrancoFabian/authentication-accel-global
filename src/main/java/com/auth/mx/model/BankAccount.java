package com.auth.mx.model;

import com.auth.mx.security.EncryptionUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String encryptedAccountNumber;

    @Column(nullable = false)
    private String encryptedAESKey;

    @Column(precision = 19, scale = 4)
    private BigDecimal balance;

    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @Transient
    private RSAPublicKey publicKey;

    @Transient
    private RSAPrivateKey privateKey;

    public void setAccountNumber(String accountNumber) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Public key must be set before encryption");
        }
        javax.crypto.SecretKey aesKey = EncryptionUtil.generateAESKey();
        this.encryptedAccountNumber = EncryptionUtil.encryptWithAES(accountNumber, aesKey);
        this.encryptedAESKey = EncryptionUtil.encryptAESKeyWithRSA(aesKey, publicKey);
    }

    public String getAccountNumber() throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Private key must be set before decryption");
        }
        javax.crypto.SecretKey aesKey = EncryptionUtil.decryptAESKeyWithRSA(encryptedAESKey, privateKey);
        return EncryptionUtil.decryptWithAES(encryptedAccountNumber, aesKey);
    }
}
