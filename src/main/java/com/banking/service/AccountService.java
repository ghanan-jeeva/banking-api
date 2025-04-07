package com.banking.service;

import com.banking.exception.BankingException;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private static final int MAX_RETRIES = 3;

    @Transactional
    public Account createAccount(String accountHolderName, BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankingException("Initial balance cannot be negative");
        }
        Account account = new Account(accountHolderName, initialBalance);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException("Account not found: " + accountNumber));
    }

    @Transactional
    public Transaction transferMoney(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new BankingException("Cannot transfer to the same account");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Transfer amount must be positive");
        }

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return executeTransfer(sourceAccountNumber, destinationAccountNumber, amount);
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts == MAX_RETRIES) {
                    throw new BankingException("Failed to complete transfer after " + MAX_RETRIES + " attempts");
                }
            }
        }
        throw new BankingException("Failed to complete transfer");
    }

    private Transaction executeTransfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new BankingException("Source account not found: " + sourceAccountNumber));
        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new BankingException("Destination account not found: " + destinationAccountNumber));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient funds in account: " + sourceAccountNumber);
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDescription("Transfer from " + sourceAccountNumber + " to " + destinationAccountNumber);

        sourceAccount.getTransactions().add(transaction);
        destinationAccount.getTransactions().add(transaction);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        return transaction;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException("Account not found: " + accountNumber));
        return account.getTransactions();
    }
} 