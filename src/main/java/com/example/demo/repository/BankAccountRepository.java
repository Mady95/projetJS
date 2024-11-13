package com.example.demo.repository;

import com.example.demo.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    BankAccount findByIdAndUserId(Long id, Long userId);
    List<BankAccount> findByUserId(Long userId);
}
