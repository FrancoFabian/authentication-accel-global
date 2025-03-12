package com.auth.mx.repository;

import com.auth.mx.model.BankAccount;
import com.auth.mx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUser(User user);
}
