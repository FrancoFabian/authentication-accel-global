package com.auth.mx.service;
import com.auth.mx.model.BankAccount;
import com.auth.mx.model.Transaction;
import com.auth.mx.model.User;
import com.auth.mx.repository.BankAccountRepository;
import com.auth.mx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    @Transactional
    public BankAccount createAccount(String userEmail, String accountNumber) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BankAccount account = BankAccount.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .publicKey(publicKey)
                .build();

        account.setAccountNumber(accountNumber);
        return bankAccountRepository.save(account);
    }

    public List<BankAccount> getUserAccounts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<BankAccount> accounts = bankAccountRepository.findByUser(user);
        accounts.forEach(account -> {
            account.setPrivateKey(privateKey);
            account.setPublicKey(publicKey);
        });

        return accounts;
    }

    @Transactional
    public Transaction deposit(Long accountId, BigDecimal amount, String userEmail) {
        BankAccount account = getAccountWithUserValidation(accountId, userEmail);

        account.setBalance(account.getBalance().add(amount));
        Transaction transaction = Transaction.builder()
                .bankAccount(account)
                .amount(amount)
                .type(Transaction.TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .balanceAfterTransaction(account.getBalance())
                .description("Deposit")
                .build();

        account.getTransactions().add(transaction);
        bankAccountRepository.save(account);
        return transaction;
    }

    @Transactional
    public Transaction withdraw(Long accountId, BigDecimal amount, String userEmail) {
        BankAccount account = getAccountWithUserValidation(accountId, userEmail);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Transaction transaction = Transaction.builder()
                .bankAccount(account)
                .amount(amount)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .balanceAfterTransaction(account.getBalance())
                .description("Withdrawal")
                .build();

        account.getTransactions().add(transaction);
        bankAccountRepository.save(account);
        return transaction;
    }

    private BankAccount getAccountWithUserValidation(Long accountId, String userEmail) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new IllegalStateException("User does not own this account");
        }

        return account;
    }
}
