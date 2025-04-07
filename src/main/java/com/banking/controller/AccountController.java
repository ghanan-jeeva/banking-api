package com.banking.controller;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getAccountHolderName(), request.getInitialBalance());
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transferMoney(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = accountService.transferMoney(
            request.getSourceAccountNumber(),
            request.getDestinationAccountNumber(),
            request.getAmount()
        );
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountNumber) {
        List<Transaction> transactions = accountService.getTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @Data
    static class CreateAccountRequest {
        @NotBlank(message = "Account holder name is required")
        private String accountHolderName;

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0", message = "Initial balance must be positive")
        private BigDecimal initialBalance;

        public String getAccountHolderName() {
            return accountHolderName;
        }

        public void setAccountHolderName(String accountHolderName) {
            this.accountHolderName = accountHolderName;
        }

        public BigDecimal getInitialBalance() {
            return initialBalance;
        }

        public void setInitialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
        }
    }

    @Data
    static class TransferRequest {
        @NotBlank(message = "Source account number is required")
        private String sourceAccountNumber;

        @NotBlank(message = "Destination account number is required")
        private String destinationAccountNumber;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        private BigDecimal amount;

        public String getSourceAccountNumber() {
            return sourceAccountNumber;
        }

        public void setSourceAccountNumber(String sourceAccountNumber) {
            this.sourceAccountNumber = sourceAccountNumber;
        }

        public String getDestinationAccountNumber() {
            return destinationAccountNumber;
        }

        public void setDestinationAccountNumber(String destinationAccountNumber) {
            this.destinationAccountNumber = destinationAccountNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
} 