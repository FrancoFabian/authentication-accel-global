package com.auth.mx.controller;

import com.auth.mx.dto.BankAccountRequest;
import com.auth.mx.dto.TransactionRequest;
import com.auth.mx.model.BankAccount;
import com.auth.mx.model.Transaction;
import com.auth.mx.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    // Endpoint para crear una cuenta bancaria
    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createAccount(@RequestParam("email") String email,
                                                     @RequestBody BankAccountRequest accountRequest) throws Exception {
        BankAccount account = bankAccountService.createAccount(email, accountRequest.getAccountNumber());
        return ResponseEntity.ok(account);
    }

    // Endpoint para obtener las cuentas del usuario
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAccounts(@RequestParam("email") String email) {
        List<BankAccount> accounts = bankAccountService.getUserAccounts(email);
        return ResponseEntity.ok(accounts);
    }

    // Endpoint para realizar un depósito
    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<Transaction> deposit(@PathVariable("id") Long accountId,
                                               @RequestParam("email") String email,
                                               @RequestBody TransactionRequest transactionRequest) {
        Transaction transaction = bankAccountService.deposit(accountId, transactionRequest.getAmount(), email);
        return ResponseEntity.ok(transaction);
    }

    // Endpoint para realizar un retiro
    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<Transaction> withdraw(@PathVariable("id") Long accountId,
                                                @RequestParam("email") String email,
                                                @RequestBody TransactionRequest transactionRequest) {
        Transaction transaction = bankAccountService.withdraw(accountId, transactionRequest.getAmount(), email);
        return ResponseEntity.ok(transaction);
    }
}
